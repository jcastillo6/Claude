# Staff Engineer Mentorship Program

## Goal
Reach **staff engineer level** at a top-tier company (FAANG/fintech/high-scale).
Current level: **Strong Senior Engineer — 88% to Staff** (updated session 2).

---

## Who You Are
- Strong Java backend engineer with fintech background
- Solid concurrency instincts — finds race conditions, TOCTOU, non-atomic operations
- Good system design direction — always arrives at the right architecture
- Security awareness strong — SQL injection, path traversal, IDOR, file upload, GDPR/PCI-DSS
- Leadership instinct — pushes back on unrealistic timelines, thinks incrementally
- Real system experience: AML transaction monitoring system (2M tx/day, Pub/Sub, stream processing)
- Weakness: precision on fixes (finds bugs, misses the specific mechanism), failure mode enumeration not yet automatic, business impact framing still emerging

---

## The Three Remaining Gaps to Staff Level

### Gap 1 — Name the Fix, Not Just the Bug
You consistently find issues but stop before naming the specific fix.
- "Race condition" → not enough. Need: "Fix is a partial unique index WHERE status = 'PENDING'"
- "HTTP in transaction" → not enough. Need: "Hold connection during HTTP = pool exhaustion at load"
- Finding the bug is 40% of credit. The mechanism + fix is 60%.

### Gap 2 — Design for Failure Before Success (Biggest Gap)
Every system design component needs a defined failure behavior BEFORE you describe the happy path.
- For every service: what happens when it is unavailable?
- For every cache: what happens when Redis loses quorum?
- For every external API: what if it never responds?
- The staff move: degraded mode + circuit breaker + pre-agreed rollback trigger

### Gap 3 — Come With a Recommendation, Not Just a Problem
In behavioral questions: every escalation needs a concrete recommendation attached.
- "We need to renegotiate" → not enough. Need: "I recommend the executive calls the customer and offers X"
- "I disagree" → not enough. Need: "I will document the risks in writing and we agree: if error rate > 1% in 30 min, we roll back without debate"

---

## Mock Interview Result (Session 2)

**Verdict: No Hire — at the threshold (88% to staff)**

| Section | Score | Benchmark |
|---|---|---|
| Code Review | 70% | Staff minimum: 80% |
| System Design | 55% | Staff minimum: 75% |
| Behavioral | 65% | Staff minimum: 75% |

System design is the weakest section. Failure mode enumeration cost the most points.

---

## Session Log

### Session 1 (2026-08-20) — Starting Level: 80% to Staff
- Code review of order processing system — missed N+1, broken synchronized, transaction management
- 8 certification questions — Java concurrency deep dive
- 5 system design questions — payment, inventory, news feed, microservices, real-time inventory
- 12 code review questions — Spring, JWT, file upload, search, payment

### Session 2 (2026-08-20 to 2026-09-09) — Current Level: 88% to Staff
- Q29: Behavioral — technical disagreement (in-memory vs PostgreSQL fraud rules)
- Q30: Distributed systems code review — HTTP in @Transactional, outbox pattern, idempotency
- Q31: Observability code review — correlation IDs, MDC, circuit breaker, metrics, stack traces
- Q32: Incident response — triage, communication, mitigation vs root cause, post-mortem
- Q33: CAP theorem applied — flash sale inventory, CP vs AP, slot partitioning, Redis DECR
- Q34: Behavioral — mentoring (direct feedback vs retro proxy, escalation warning before manager)
- Q35: Exactly-once semantics — Kafka consumer, idempotent consumer, outbox pattern, auto-commit
- Q36: Regulatory code review — GDPR Art. 5/17/33, PCI-DSS CVV vs PAN, encryption at rest
- Q37: Read-your-writes consistency — replica lag, three fix approaches, optimistic UI
- Q38: "Tell me about a system you designed" — AML system, business stakes framing
- Q39: 2PC vs Saga — choreography vs orchestration, non-reversible steps (email goes last)
- Q40: Mock full interview — No Hire verdict at 88% threshold
- Q41: Distributed job scheduler — failure modes AND architecture complete (see below). Full staff-level answer reached through precision pushes.
- Q42: Postgres concurrency deep dive (unscheduled detour, triggered by student's own question) — row locking on UPDATE, lock held for transaction lifetime not statement lifetime, MVCC/non-repeatable reads under READ COMMITTED, practical NTP implementation (chrony config, UTC end-to-end, timeout sizing methodology)
- Q43: Behavioral — cross-team collaboration without authority — INCOMPLETE, student ended the exercise before completing the final precision step (see detail below). Revealed Gap 3 is currently weaker than Gap 1/2.

---

## Q41 Detail — Distributed Job Scheduler (Failure Modes + Architecture) — COMPLETE

Question: Design a distributed job scheduler. Jobs must run exactly once. 1M scheduled jobs. Workers can crash at any time. Jobs take 100ms to 30 minutes. Answer failure modes FIRST, then architecture.

Outcome: all 5 failure modes reached correct final mechanisms, but only after repeated precision pushes — first-pass answers consistently named the right *shape* of solution but stopped short of the specific mechanism, and needed 3-5 follow-ups each. Notably self-committed a check-then-act (TOCTOU) mistake twice in this session, in the student's own proposed design — the same bug class they're strong at catching in *other people's* code review.

| # | Failure Mode | First-Pass Answer | Precision Gap | Final Mechanism |
|---|---|---|---|---|
| 1 | Worker crashes mid-job, duplicate execution | "restart the work, might create duplicates" | Detection window unspecified; dedup mechanism unspecified | Heartbeat every 1s, 10s lease timeout decoupled from the 30-min job max → fencing token incremented on reassignment, checked at write time → idempotency key = job ID sent to the external system, which does check-and-record as one atomic operation (unique-constraint insert), never a worker-side read-then-write |
| 2 | Scheduler crashes | "elect a new leader" | Where the schedule lives during the crash; the election primitive | Schedule persisted in durable storage (DB), not scheduler memory → leader election via lease/fencing token in a coordination service (etcd/ZooKeeper/DB lease row) — same pattern as worker heartbeats, one level up |
| 3 | Clock skew between nodes | "wrong start time" (symptom, not fix) | No fix named on first pass | NTP bounds skew to a known error margin (does not eliminate it) + UTC everywhere + timeouts designed with margin above worst-case drift. TrueTime (self-named) is the same idea taken to a provable bound, not needed here |
| 4 | Thundering herd (10K jobs at 09:00:00) | "bounded worker pool" (protects execution only) | Didn't address the burst at the trigger point | Jitter at schedule time (spread fire times across a window) + rate-limited/batched dispatch — same idea as the student's own Q33 flash-sale answer — bounded pool as second line of defense |

**Generalized principle surfaced this session:** exactly-once anywhere in a distributed system requires the check-and-act to be a *single atomic operation* (unique-constraint insert / CAS), never two sequential steps — regardless of which component performs it. This is the same insight as the student's existing TOCTOU knowledge, now explicitly connected to external-system idempotency (payment gateway) and to schedule/leader-election design, not just in-process concurrency.

### Architecture (completed after failure modes)

Final design, built through precision pushes rather than reached on first pass:
- Schedule persisted durably (DB), not in coordinator memory
- 1M jobs sharded via **consistent hashing with virtual nodes** across multiple coordinator instances — student correctly explained why consistent hashing beats plain `hash % N` (minimal reshuffle on node add/remove) once pushed, and correctly identified that virtual nodes solve uneven load distribution with few physical nodes
- Each shard independently leased/elected (student correctly identified, unprompted on the second try, that ring membership alone does NOT guarantee single ownership — still needs a lease per shard, same pattern as global leader election)
- Dispatch via Kafka; **student correctly resolved a self-introduced redundancy** — initially had both a custom coordinator heartbeat AND Kafka's own consumer-group liveness protocol tracking worker health, then correctly chose to rely on Kafka's built-in protocol instead of duplicating it, when asked to justify keeping both
- Idempotency key (job ID) + unique-constraint atomic DB insert kept as defense-in-depth even after delegating liveness to Kafka — correctly reasoned that a straggling worker mid-job during a Kafka rebalance is the same zombie-worker risk as before, just triggered by `session.timeout.ms` instead of a custom heartbeat
- Jitter at schedule time for thundering herd, correctly distinguished from the fencing-token mechanism that prevents race conditions (self-corrected a conflation between the two on first pass)
- NTP + UTC + timeouts sized against measured worst-case, not assumption

**Score vs. Gap 2 (Design for Failure Before Success):** strong session. This is the clearest evidence yet of closing this gap — the student not only enumerated failure modes before architecture (as instructed) but caught two self-introduced design flaws (a check-then-act race, a redundant liveness mechanism) when pushed to justify decisions, rather than needing them pointed out directly. That self-audit reflex, applied to their own design rather than someone else's code, is the actual staff-level behavior this gap was tracking.

---

## Q43 Detail — Behavioral: Cross-Team Collaboration Without Authority — INCOMPLETE, closed early by student

Scenario: blocked on a schema change owned by another team (payments), no authority over their roadmap, tech lead noncommittal, own deadline in 6 weeks.

Progression across the exercise (each step required 2-3 precision pushes):
1. First pass: escalation with no concrete ask ("I need your support to handle this case") — Gap 3 in its original form
2. Pushed to specific ask + date: correctly landed on "manager contacts payments manager directly" + "15 days" as a hard trigger point
3. Fallback: initially just labeled "solution B" with no content; pushed twice before naming the real option (ship the rest of the feature, delay only the affected rule, accept a defined coverage gap)
4. Risk ownership: correctly identified — unprompted on this one — that accepting a fraud-coverage gap is a risk-acceptance decision, not a pure engineering call, and named "compliance" as the approver (a real, non-generic answer, unlike the earlier "business needs to approve")
5. **Final ask — write the actual sentence to compliance, with the specifics filled in** — student first left my own placeholder brackets (`[field]`, `[specific fraud pattern]`, `[reason]`, `[other option]`) unfilled, verbatim. Given one more explicit chance to fill them in with real content, the student instead ended the exercise ("next exercise") rather than complete it.

**Score vs. Gap 3: weaker than Gap 1/Gap 2 performance on the same session.** In Q41 (technical), the student closed every precision loop when pushed, including on the 3rd-4th follow-up. In Q43 (behavioral), the pattern of naming that someone-should-act without naming who/what recurred three separate times (manager support, fallback content, business approval), and the exercise ended before the final, most concrete step — an actual sentence with real specifics — was produced. This is a genuine, not cosmetic, difference: the technical precision habit does not yet transfer to behavioral/interpersonal precision under the same kind of pressure. Priority for next session.

---

## Where to Resume Next Session

**Re-attempt Q43's final step first, briefly** — before moving to new material, have the student write one complete, specific sentence to a named stakeholder about a real risk trade-off, with no placeholders. This is the single most important open item from this session: closing the Gap 1→Gap 3 transfer gap.

**Then continue with:**
- System design: distributed search OR real-time leaderboard
- Repeat mock interview — target system design failure modes specifically; this session suggests the student is close to closing Gap 2, so a fresh full mock (all 3 sections) is worth prioritizing soon to check if the score moved off the 88%/No-Hire threshold
- If time allows: revisit Postgres isolation levels (Q42) briefly — student initially got READ COMMITTED non-repeatable-read behavior backwards (thought a second read in the same transaction would NOT see another transaction's intervening commit) before self-correcting when pushed; worth one quick check-question next session to confirm it stuck

---

## Topics Mastered

### Java Concurrency
- volatile — visibility guarantee, not atomicity
- synchronized — instance vs class lock, self-invocation
- CAS — compareAndSet, AtomicBoolean, AtomicLong
- TOCTOU — check-then-act race conditions
- ConcurrentHashMap — per-bucket locking, computeIfPresent atomicity
- ReentrantReadWriteLock — read/write semantics, no lock upgrading
- CompletableFuture — exception capture, allOf, dedicated executor
- ExecutorService — bounded queues, CallerRunsPolicy, virtual threads
- Double-checked locking — volatile + synchronized pattern
- WeakHashMap — weak references, use cases and dangers

### Financial / API Design
- BigDecimal — string constructor, RoundingMode.HALF_UP, scale
- Instant vs LocalDateTime — always UTC in financial systems
- Idempotency keys — UUID, DB unique constraint, 409 Conflict
- Dual write problem — outbox pattern, saga, compensation
- Optimistic locking — @Version, OptimisticLockException

### Spring / JPA
- @Transactional propagation — REQUIRED, REQUIRES_NEW, connection cost
- Self-invocation proxy bypass — three fix options
- N+1 query — JOIN FETCH, @EntityGraph, DTO projection
- Lazy loading — never in a loop, fetch at query time
- Read replicas — replication lag, read-your-writes
- HTTP call inside @Transactional — connection pool exhaustion (P0 pattern)

### Security (Identified on First Pass)
- SQL injection — PreparedStatement, parameterized queries
- Path traversal — normalize(), startsWith() validation
- IDOR — never trust client-supplied identity, verify ownership before returning
- User enumeration — timing attacks, consistent responses
- JWT — HS256 vs RS256, weak keys, no revocation
- File upload — magic bytes, quarantine, executable upload (RCE)
- Sort injection — ORDER BY whitelist
- Wildcard enumeration — % in LIKE queries
- SSN/PII in logs — GDPR violation
- CVV storage — PCI-DSS absolute prohibition, never store
- PAN (card number) — tokenize via gateway, never store raw

### Regulatory
- GDPR Article 5 — data minimization, purpose limitation, lawful basis
- GDPR Article 17 — right to erasure across ALL systems (not just users table)
- GDPR Article 33 — breach notification within 72 hours
- PCI-DSS Requirement 3.2 — CVV never stored, ever
- PCI-DSS Requirement 3.4 — PAN tokenized or AES-256 encrypted, key stored separately

### Observability
- Correlation ID / Trace ID — MDC, set at entry, clear in finally
- Structured logging — parameterized {}, never string concatenation
- Stack traces — log.error("msg", e) not log.error("msg: " + e.getMessage())
- Metrics — Micrometer counters by outcome, latency histograms by success/failure separately
- Circuit breaker — Resilience4j, fail fast, degraded mode
- Distributed tracing — W3C traceparent, B3 headers, cross-service correlation
- Dead letter queue — @RetryableTopic, failed messages never silently dropped

### Distributed Systems
- CAP theorem — CP vs AP, partition tolerance is not optional
- Read-your-writes consistency — three fix approaches (primary read, return in response, lag check)
- Replication lag — async replication, TOCTOU at distributed scale
- Exactly-once semantics — idempotent consumer + at-least-once delivery
- Kafka consumer — manual offset commit, DLQ, idempotency key, auto-commit danger
- Outbox pattern — DB + Kafka atomic via outbox table + Debezium/polling
- 2PC vs Saga — 2PC blocks on coordinator crash, Saga uses compensating transactions
- Choreography vs orchestration — event-driven vs centralized coordinator
- Non-reversible steps in Saga — email always goes last
- Slot partitioning — hot product inventory, Redis DECR per slot
- Feature store — batch features (offline) + real-time features (inline), 200ms budget
- Fail open vs fail closed — explicit tradeoff decision for every external dependency
- Degraded mode — run what you can, skip what requires unavailable dependencies
- Transaction probing — escalating amounts, new merchant detection via real-time velocity
- Fencing tokens — monotonically increasing token per lease grant, checked at write time to reject stale writes from a reassigned worker
- Lease-based leader election — coordination service (etcd/ZooKeeper) or DB lease row with TTL; same primitive for worker heartbeats and scheduler leader election
- Atomic check-and-set as the universal exactly-once primitive — check and record must be one indivisible operation (unique-constraint insert / CAS), never a separate read-then-write, regardless of which component performs it
- TrueTime — Spanner's bounded-uncertainty global clock (atomic clocks + GPS); NTP is the practical baseline for everyone else
- Jitter — spreading scheduled fire times across a window to prevent thundering herd at the trigger point, distinct from rate-limiting at dispatch
- Consistent hashing with virtual nodes — sharding 1M+ items across N coordinators with minimal reshuffle on node add/remove; virtual nodes fix uneven load with few physical nodes
- Kafka consumer group liveness as a reusable fencing mechanism — don't duplicate a custom heartbeat when the transport already provides membership/rebalance semantics; recognize when two mechanisms are solving the same problem
- NTP practical implementation — chrony config pointed at cloud-provider internal time source (not public pool), `chronyc tracking` / `timedatectl` for verification, offset exported as a metric with alerting, UTC end-to-end (OS via `timedatectl set-timezone UTC`, Postgres `TIMESTAMPTZ` not `TIMESTAMP`, Java `Instant`/`OffsetDateTime` not `LocalDateTime`, ISO-8601 with explicit offset on the wire), timeout margin sized from measured worst-case offset, not assumed

### Postgres Concurrency (New — Q42)
- Row-level locking — plain `UPDATE` acquires an implicit row lock automatically; no `SELECT ... FOR UPDATE` required to get blocking behavior
- Lock duration — held for the full transaction lifetime (until COMMIT/ROLLBACK), not released when the locking statement finishes; a slow operation (e.g. HTTP call) after an UPDATE holds the lock the whole time — lock contention is a distinct failure mode from connection pool exhaustion, and the two compound under load
- MVCC — plain `SELECT` never takes a lock and is never blocked by a writer; readers and writers don't block each other
- READ COMMITTED (Postgres default) — each *statement* takes a fresh snapshot at its own start, not once per transaction → non-repeatable reads are possible (a second read in the same transaction can see another transaction's intervening commit). Initially answered this backwards (assumed the value would stay frozen); self-corrected when pushed to trace through the mechanism statement-by-statement — worth a quick confirmation check next session
- REPEATABLE READ / SERIALIZABLE — one snapshot for the whole transaction, taken at its first statement; prevents non-repeatable reads
- Practical fix for read-then-write logic under READ COMMITTED — collapse into a single atomic statement (e.g. `UPDATE ... SET balance = balance - 10 WHERE balance >= 10`) or use explicit `SELECT ... FOR UPDATE`, rather than relying on isolation level alone

### System Design Patterns
- Saga pattern — compensating transactions
- Outbox pattern — guaranteed delivery, Debezium CDC
- Idempotent consumer — exactly-once at application layer
- CDC (Change Data Capture) — Debezium, WAL, Kafka
- Strangler Fig — incremental migration
- Snapshot-and-drain — lock-free batch processing
- Hybrid push-pull — news feed, celebrity problem
- Redis ZSET — feed storage, sorted by timestamp
- Flash sale — Redis DECR, slot partitioning, thundering herd
- Circuit breaker — per-provider, half-open state, degraded mode
- Rate limiting — token bucket, sliding window, Redis INCR

### Behavioral / Leadership
- Situation-Impact-Request framework — feedback delivery
- Disagree and commit — measurable exit criterion before committing
- Escalation with recommendation — never just a problem, always attach a recommendation
- Direct feedback before retro — retro is not a proxy for one person's behavior
- ADR (Architecture Decision Record) — decision + success criteria + revisit trigger
- Incident response protocol — communicate first, investigate second, update every 15 min
- Post-mortem structure — blameless, timeline, impact quantified, contributing factors, action items with owner + date
- "Tell me about a system you designed" — open with business stakes, not technical constraints
- 3-week vs 8-week timeline — document risk decision before building, pre-agree rollback threshold

---

## Recommended Reading
1. **Java Concurrency in Practice** — Goetz
2. **Designing Data-Intensive Applications** — Kleppmann — chapters 5, 7, 9 highest priority
3. **OWASP Top 10** — owasp.org
4. **Clean Architecture** — Martin
5. **PCI-DSS Summary** — one day — know requirements 3.2 and 3.4 cold
6. **GDPR Summary** — Articles 5, 17, 33 specifically
7. **Google SRE Book** — Chapter 13 (Emergency Response), Chapter 15 (Postmortem Culture) — free online

---

## Rules For The Mentor (Claude)

1. Never give the answer before the student has tried
2. Always push for precision — "be more specific" until the mechanism is named
3. Always ask for business impact on every P0
4. Score honestly — do not inflate assessments
5. Apply the five auth questions to every security question:
   - Can an attacker enumerate users?
   - Can an attacker enumerate emails?
   - Can tokens be predicted, reused, or stolen?
   - Can an attacker access another user's data?
   - What happens with no input, malicious input, or oversized input?
6. Track progress across sessions — compare first-pass scores over time
7. Final goal: hire verdict at a top-tier staff engineer interview
8. Push "what happens when X is unavailable?" for every system design component
9. Push "what is the specific fix?" for every bug identified — finding the bug is 40%, the fix is 60%
10. For behavioral: push "what exactly did you say / what exactly do you recommend?"
