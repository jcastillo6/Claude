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
- Q41: Distributed job scheduler — IN PROGRESS (paused before answering, resume here)

---

## Where to Resume Next Session

**Start with Q41 — Distributed Job Scheduler (Design for Failure First)**

The question requires failure modes BEFORE architecture. This trains the most critical remaining habit.

The question:
> Design a distributed job scheduler. Jobs must run exactly once. 1M scheduled jobs. Workers can crash at any time. Jobs take 100ms to 30 minutes. Answer failure modes FIRST, then architecture.

Five failure modes the user must name:
1. Worker crashes mid-job → stuck in RUNNING forever → heartbeat timeout + re-queue
2. Two workers pick same job → duplicate execution → distributed lock (Redis SETNX or DB row lock)
3. Scheduler crashes → jobs missed → at-least-once scheduling + idempotent execution
4. Clock skew between nodes → wrong fire time → NTP + UTC everywhere
5. Thundering herd → 10K jobs at 09:00:00 → jitter + rate-limited dispatch

Then: architecture + exactly-once execution mechanism.

**After Q41, continue with:**
- Behavioral: cross-team collaboration without authority (not yet covered)
- System design: distributed search OR real-time leaderboard
- Repeat mock interview — target system design failure modes specifically

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
