# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table customer (
  id                        bigint not null,
  fname                     varchar(255) not null,
  lname                     varchar(255) not null,
  account_number            varchar(255),
  address                   varchar(255),
  mobile                    varchar(255),
  email                     varchar(255),
  signature_one             varchar(255) not null,
  signature_two             varchar(255) not null,
  constraint pk_customer primary key (id))
;

create table customer_signature (
  id                        bigint not null,
  accno                     varchar(255),
  imagename                 varchar(255),
  angles                    varchar(255),
  constraint pk_customer_signature primary key (id))
;

create table employee (
  id                        bigint not null,
  fname                     varchar(255) not null,
  lname                     varchar(255) not null,
  username                  varchar(255) not null,
  password                  varchar(255) not null,
  email                     varchar(255) not null,
  mobile                    integer,
  role                      varchar(255) not null,
  constraint uq_employee_1 unique (username),
  constraint pk_employee primary key (id))
;

create sequence customer_seq;

create sequence customer_signature_seq;

create sequence employee_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists customer;

drop table if exists customer_signature;

drop table if exists employee;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists customer_seq;

drop sequence if exists customer_signature_seq;

drop sequence if exists employee_seq;

