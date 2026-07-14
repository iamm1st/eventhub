# EventHub Database Design

## 1. Overview

EventHub uses a relational PostgreSQL database. The database is designed for a REST API application where users can create events, buy tickets, register for events, leave reviews, and where administrators can manage users and platform data.

The database is normalized and contains separate tables for users, roles, events, categories, locations, tickets, registrations, reviews, payments, and audit logs.

The planned database contains 12 tables:

* `users`
* `roles`
* `user_roles`
* `organizer_applications`
* `event_categories`
* `locations`
* `events`
* `ticket_types`
* `registrations`
* `reviews`
* `payments`
* `audit_logs`

---

## 2. Main Tables

### users

The `users` table stores all registered users of the system.

It includes regular users, event organizers, and administrators.

Main fields:

* `id` — primary key
* `username` — unique username
* `email` — unique email address
* `password` — encrypted password
* `status` — user status: `ACTIVE` or `BLOCKED`
* `created_at` — creation date
* `updated_at` — last update date

The `status` field is used to block users if necessary. A blocked user will not be able to buy tickets, create events, or leave reviews.

---

### roles

The `roles` table stores system roles.

Main roles:

* `ROLE_USER`
* `ROLE_ORGANIZER`
* `ROLE_ADMIN`

The role name must be unique.

---

### user_roles

The `user_roles` table is a join table between `users` and `roles`.

It is used for a many-to-many relationship, because one user can have more than one role. For example, one account can be both a regular user and an organizer.

Main fields:

* `user_id` — foreign key to `users`
* `role_id` — foreign key to `roles`

The combination of `user_id` and `role_id` is unique.

---

### organizer_applications

The `organizer_applications` table stores user requests for becoming event organizers.

A regular user cannot become an organizer just by clicking a button. The user must submit an application with additional information, such as organization name, contact details, description, and website or social link. The administrator reviews the application and either approves or rejects it.

Main fields:

- `id` — primary key
- `user_id` — foreign key to `users`
- `organization_name` — organization or public name
- `contact_email` — contact email
- `contact_phone` — optional contact phone
- `description` — information about the organizer
- `website_url` — optional website or social media link
- `status` — application status
- `admin_comment` — optional administrator comment
- `created_at`
- `updated_at`
- `reviewed_at`

Possible application statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`

When an application is approved, the user receives the `ROLE_ORGANIZER` role. The user keeps the `ROLE_USER` role and can still buy tickets for other users' events. However, an organizer cannot buy tickets for their own events.

---

### event_categories

The `event_categories` table stores event categories.

Examples:

* IT
* Music
* Sport
* Education
* Business
* Art

Main fields:

* `id` — primary key
* `name` — unique category name
* `created_at`
* `updated_at`

Categories are stored separately to avoid duplicating category names in the `events` table.

---

### locations

The `locations` table stores places where events are held.

Main fields:

* `id` — primary key
* `country`
* `city`
* `address`
* `place_name`
* `created_at`
* `updated_at`

One location can be used by several events.

---

### events

The `events` table is the main table of the system. It stores information about events created by organizers.

Main fields:

* `id` — primary key
* `title` — event title
* `description` — event description
* `start_date` — event start date and time
* `end_date` — event end date and time
* `capacity` — maximum number of participants
* `status` — event status
* `rating` — average event rating
* `organizer_id` — foreign key to `users`
* `category_id` — foreign key to `event_categories`
* `location_id` — foreign key to `locations`
* `created_at`
* `updated_at`

Possible event statuses:

* `DRAFT`
* `PUBLISHED`
* `CANCELLED`
* `FINISHED`

The event capacity must be greater than zero. The rating must be between 0 and 5.

---

### ticket_types

The `ticket_types` table stores different types of tickets for an event.

For example:

* Standard
* VIP
* Premium

Main fields:

* `id` — primary key
* `event_id` — foreign key to `events`
* `name` — ticket type name
* `price` — ticket price
* `total_quantity` — total number of tickets of this type
* `available_quantity` — number of available tickets
* `created_at`
* `updated_at`

Important constraints:

* price must be greater than or equal to 0
* total quantity must be greater than 0
* available quantity must be greater than or equal to 0
* available quantity cannot be greater than total quantity
* one event cannot have two ticket types with the same name

The total number of tickets for one event must not exceed the event capacity. This rule will be checked in the service layer.

---

### registrations

The `registrations` table stores information about users registered for events.

A registration is created when a user buys a ticket.

Main fields:

* `id` — primary key
* `user_id` — foreign key to `users`
* `event_id` — foreign key to `events`
* `ticket_type_id` — foreign key to `ticket_types`
* `status` — registration status
* `registration_date`
* `cancelled_at`
* `created_at`
* `updated_at`

Possible registration statuses:

* `ACTIVE`
* `CANCELLED`

A user cannot have two active registrations for the same event.

---

### reviews

The `reviews` table stores user reviews for events.

Main fields:

* `id` — primary key
* `user_id` — foreign key to `users`
* `event_id` — foreign key to `events`
* `rating` — rating from 1 to 5
* `comment` — review text
* `created_at`
* `updated_at`

Important constraints:

* rating must be between 1 and 5
* one user can leave only one review for one event

The application will also check that a review can be created only by a user who was registered for the event.

---

### payments

The `payments` table stores simulated payment information.

The project does not use a real payment provider. Payment logic is implemented as a simulation for educational purposes.

Main fields:

* `id` — primary key
* `registration_id` — foreign key to `registrations`
* `amount` — payment amount
* `status` — payment status
* `created_at`
* `updated_at`

Possible payment statuses:

* `PENDING`
* `PAID`
* `FAILED`
* `REFUNDED`

One registration has one payment.

---

### audit_logs

The `audit_logs` table stores important user actions.

It will be used together with AOP logging.

Main fields:

* `id` — primary key
* `username` — user who performed the action
* `action` — action name
* `entity_type` — entity type
* `entity_id` — entity identifier
* `success` — whether the action was successful
* `error_message` — error message if the action failed
* `execution_time_ms` — method execution time
* `created_at`

Examples of logged actions:

* creating an event
* buying a ticket
* cancelling a registration
* creating a review
* blocking a user
* deleting a review

---

## 3. Main Relationships

The database contains the following main relationships:

* `users` many-to-many `roles` through `user_roles`
* `users` one-to-many `events`
* `event_categories` one-to-many `events`
* `locations` one-to-many `events`
* `events` one-to-many `ticket_types`
* `users` one-to-many `registrations`
* `events` one-to-many `registrations`
* `ticket_types` one-to-many `registrations`
* `users` one-to-many `reviews`
* `events` one-to-many `reviews`
* `registrations` one-to-one `payments`

These relationships will be implemented in Java using JPA and Hibernate annotations such as `@OneToMany`, `@ManyToOne`, `@ManyToMany`, and `@OneToOne`.

---

## 4. Database Constraints

The database will include:

* primary keys
* foreign keys
* unique constraints
* not null constraints
* check constraints
* indexes

Examples of important constraints:

* user email must be unique
* username must be unique
* role name must be unique
* category name must be unique
* ticket price cannot be negative
* ticket quantity cannot be negative
* review rating must be from 1 to 5
* event rating must be from 0 to 5
* one user cannot leave two reviews for the same event
* one user cannot have two active registrations for the same event

---

## 5. Business Rules

Some rules will be checked in the Java service layer, because they depend on business logic.

Main business rules:

* blocked users cannot buy tickets, create events, or leave reviews
* only organizers can create events
* an organizer can edit only their own events
* a user can cancel only their own registration
* tickets can be bought only for published events
* tickets cannot be bought for cancelled or already started events
* ticket availability must be checked before registration
* ticket quantity must decrease after successful purchase
* ticket quantity must increase after registration cancellation
* payment status must change to `REFUNDED` after cancellation
* event rating must be recalculated after creating, updating, or deleting a review
* total ticket quantity for one event must not exceed event capacity

---

## 6. Flyway Migrations

The database schema will be created using Flyway migrations.

Migration files will be stored in:

```text
src/main/resources/db/migration
```

Planned migration files:

```text
V1__create_roles_table.sql
V2__create_users_table.sql
V3__create_user_roles_table.sql
V4__create_organizer_applications_table.sql
V5__create_event_categories_table.sql
V6__create_locations_table.sql
V7__create_events_table.sql
V8__create_ticket_types_table.sql
V9__create_registrations_table.sql
V10__create_reviews_table.sql
V11__create_payments_table.sql
V12__create_audit_logs_table.sql
V13__insert_initial_data.sql
```

Flyway will automatically create and update the database structure when the application starts.
