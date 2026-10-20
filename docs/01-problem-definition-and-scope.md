# Task 1 – Problem Definition and Scope

## 1. Real-time need

Prana Studio is a small yoga studio with six instructors and about 40 classes a week. Today, bookings
happen over phone calls, WhatsApp groups and a paper register at the front desk. This causes:

| Pain point | Who feels it | Effect today |
|---|---|---|
| No live view of seats left | Members | People turn up to full classes, or skip classes that had space |
| Bookings scattered across WhatsApp, calls and paper | Front desk | 30–45 min/day spent reconciling lists; double bookings |
| No confirmation or reference | Members | "Did my booking go through?" follow-up calls |
| Cancellations not recorded | Instructors | Seats stay "taken" by no-shows; waiting members miss out |
| No status history | Studio owner | Cannot measure demand per class or instructor |

## 2. Problem statement

> Members of Prana Studio cannot see real-time class availability or self-manage their bookings,
> so the studio loses seats to double bookings and unrecorded cancellations, and staff spend
> 30–45 minutes per day reconciling bookings by hand. We will build a web-based Yoga Class
> Booking Portal that lets members register, view live slot availability, request and confirm
> a booking, cancel it, and track its status by reference. It will be delivered through an
> automated DevOps pipeline (Git → Jenkins → Selenium → Tomcat/Docker → Ansible).

## 3. Target users

* **Members (primary)**: working adults aged 20–55 who book 1–3 classes a week, mostly on mobile.
* **Front-desk staff (secondary)**: check the day's bookings and answer status queries.
* **Instructors (secondary)**: need an accurate headcount before class.

## 4. Stakeholders

| Stakeholder | Role / interest | Influence |
|---|---|---|
| Studio owner (product owner) | Prioritises the backlog, approves the MVP scope | High |
| Members | End users; want fast, reliable booking | High |
| Front-desk staff | Day-to-day operators; want fewer calls | Medium |
| Instructors | Want accurate attendance | Medium |
| Developer (Gaurav Deore) | Builds the app and the pipeline | High |
| DevOps / ops (same person for the MVP) | Runs Jenkins, Tomcat, Docker, Ansible | Medium |
| Faculty guide / evaluator | Assesses DevOps practice and deliverables | High |

## 5. Objectives

1. Let a new member register and book a class in **under 60 seconds**.
2. Show **live seat counts** that are always consistent with bookings (no overbooking).
3. Give every booking a **unique reference** and a visible status (Requested → Confirmed / Cancelled).
4. Release a seat **immediately** when a booking is cancelled.
5. Automate build, test and deployment so that every commit to `main` is **built, tested and
   deployed without manual steps**, and a failing test **blocks deployment**.

## 6. Measurable success criteria

| # | Criterion | Target | How it is measured |
|---|---|---|---|
| S1 | Time for a new member to complete a booking | ≤ 60 s | Selenium journey `BookingFlowSeleniumTest` |
| S2 | Overbookings | 0 | Seat check in `YogaSlot.reserveSeat()` + unit tests |
| S3 | Seat released after cancellation | 100 % of cancellations | `CancellationSeleniumTest` |
| S4 | Critical journeys covered by automated UI tests | ≥ 5 | Jenkins JUnit report |
| S5 | Commit-to-deploy lead time | ≤ 10 min | Jenkins pipeline duration |
| S6 | Deployment blocked when tests fail | Always | Failed pipeline evidence (Task 10) |
| S7 | Re-running provisioning changes nothing | `changed=0` | Ansible idempotency log (Task 14) |
| S8 | Rollback to previous release | ≤ 2 min, health = UP | Ansible rollback log (Task 14) |

## 7. Constraints

* **Time**: one semester; MVP must be delivered within a 15-task plan.
* **Team**: a single developer who also acts as the ops engineer.
* **Budget**: ₹0. Only free/open-source tools (GitHub free, Jenkins, Tomcat, Docker, Ansible).
* **Infrastructure**: one developer laptop (macOS, Apple silicon, 24 GB RAM). The target "server"
  is a Docker container provisioned by Ansible.
* **Data**: in-memory H2 database for the MVP. Data resets on restart, which is acceptable for a demo.
* **Security**: no payments and no passwords in the MVP. Members are identified by email.
  (Authentication is listed as out of scope.)

## 8. Approved MVP scope (frozen)

**In scope (6 functions):**

1. Member registration (name, email, phone) and sign-in by email
2. Availability / slot view with live seats left
3. Booking request (holds a seat, generates a reference)
4. Booking confirmation
5. Booking cancellation (releases the seat)
6. Booking status tracking (by reference, plus a *My Bookings* list)

**Out of scope for the MVP:** online payments, passwords/OTP, instructor and admin dashboards,
waiting lists, email/SMS notifications, recurring bookings, and a persistent production database.

The MVP is split into **15 backlog tasks** (T01–T15). They are listed in
[02-agile-plan-and-devops-workflow.md](02-agile-plan-and-devops-workflow.md) and map one-to-one
to the 15 tasks of the DevOps plan.

**Scope approved by:** Studio owner (product owner) · **Frozen on:** Sprint 0 review
