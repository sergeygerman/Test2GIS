CREATE SEQUENCE "CINEMA_RESERVATION"."CINEMA_SCHEDULES_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE "CINEMA_RESERVATION"."SEATS_RESERVATION_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
COMMIT;

/*------------------------------------------------*/

/*Список кинотеатров*/
CREATE TABLE "CINEMA_RESERVATION"."CINEMAS"
(
"ID" NUMBER(10,0) NOT NULL CHECK ("ID" >= 1 AND "ID" <= 50),
"NAME" VARCHAR2(1024 CHAR) NOT NULL ENABLE,
    CONSTRAINT "CINEMAS_PK" PRIMARY KEY ("ID"),
    CONSTRAINT "CINEMAS_NAME_UNIQUE" UNIQUE ("NAME")
);

COMMIT;

COMMENT ON TABLE "CINEMA_RESERVATION"."CINEMAS" IS 'Список кинотеатров';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMAS"."ID" IS 'Уникальный идентификатор кинотеатра';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMAS"."NAME" IS 'Наименование кинотеатра';

Insert into "CINEMA_RESERVATION"."CINEMAS" (ID, NAME) values (1,'Кинотеатр 1');
Insert into "CINEMA_RESERVATION"."CINEMAS" (ID, NAME) values (2,'Кинотеатр 2');
Insert into "CINEMA_RESERVATION"."CINEMAS" (ID, NAME) values (3,'Кинотеатр 3');
Insert into "CINEMA_RESERVATION"."CINEMAS" (ID, NAME) values (4,'Кинотеатр 4');
COMMIT;

/*------------------------------------------------*/

/*Список кинозалов*/
CREATE TABLE "CINEMA_RESERVATION"."CINEMA_HALLS" 
(
"ID" NUMBER(10,0) NOT NULL ENABLE,
"ID_CINEMAS" NUMBER(10,0) NOT NULL ENABLE, 
"HALL_NUMBER" NUMBER(10,0) NOT NULL CHECK ("HALL_NUMBER" >= 1 AND "HALL_NUMBER" <= 5),
    CONSTRAINT "CINEMA_HALLS_PK" PRIMARY KEY ("ID"),
    CONSTRAINT "CINEMA_HALLS_ID_CINEMAS_FK" FOREIGN KEY ("ID_CINEMAS")
        REFERENCES "CINEMA_RESERVATION"."CINEMAS" ("ID") ON DELETE CASCADE ENABLE,
    CONSTRAINT "CINEMA_HALLS_UNIQUE" UNIQUE ("ID_CINEMAS", "HALL_NUMBER")
);

COMMIT;

COMMENT ON TABLE "CINEMA_RESERVATION"."CINEMA_HALLS" IS 'Список залов кинотеатров';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_HALLS"."ID" IS 'Уникальный идентификатор зала';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_HALLS"."ID_CINEMAS" IS 'Ссылка на идентификатор кинотеатра, к которому относится зал';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_HALLS"."HALL_NUMBER" IS 'Номер зала кинотеатра';
COMMIT;

Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (1, 1, 1);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (2, 1, 2);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (3, 2, 1);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (4, 2, 2);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (5, 2, 3);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (6, 3, 1);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (7, 3, 2);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (8, 3, 3);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (9, 3, 4);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (10, 4, 1);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (11, 4, 2);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (12, 4, 3);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (13, 4, 4);
Insert into "CINEMA_RESERVATION"."CINEMA_HALLS" (ID, ID_CINEMAS, HALL_NUMBER) values (14, 4, 5);
COMMIT;

/*------------------------------------------------*/

/*Список мест*/
CREATE TABLE "CINEMA_RESERVATION"."SEATS"
(
"ID" NUMBER(10,0) NOT NULL ENABLE,
"ID_CINEMA_HALLS" NUMBER(10,0) NOT NULL ENABLE,
"ROW_NUM" NUMBER(10,0) NOT NULL ENABLE,
"PLACE_IN_ROW" NUMBER(10,0) NOT NULL ENABLE,
    CONSTRAINT "SEATS_PK" PRIMARY KEY ("ID"),
    CONSTRAINT "SEATS_ID_CINEMA_HALLS_FK" FOREIGN KEY ("ID_CINEMA_HALLS")
        REFERENCES "CINEMA_RESERVATION"."CINEMA_HALLS" ("ID") ON DELETE CASCADE ENABLE,
    CONSTRAINT "SEATS_UNIQUE" UNIQUE ("ID_CINEMA_HALLS", "ROW_NUM", "PLACE_IN_ROW")
);

COMMIT;

COMMENT ON TABLE "CINEMA_RESERVATION"."SEATS" IS 'Карта мест залов кинотеатров';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS"."ID" IS 'Уникальный идентификатор места';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS"."ID_CINEMA_HALLS" IS 'Ссылка на идентификатор зала';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS"."ROW_NUM" IS 'Ряд';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS"."PLACE_IN_ROW" IS 'Место';
COMMIT;

/*Универсальная карта мест для всех кинозалов - 10x10*/
declare
UID INTEGER := 1;
begin
    FOR HALL IN (select ID from CINEMA_RESERVATION.CINEMA_HALLS) loop
      FOR X IN 1..10 loop
          FOR Y IN 1..10 loop
            Insert into "CINEMA_RESERVATION"."SEATS" (ID, ID_CINEMA_HALLS, ROW_NUM, PLACE_IN_ROW) values (UID, HALL.ID, X, Y);
            UID := UID + 1;
          end loop;
      end loop;
    end loop;
  COMMIT;
end;
/

/*------------------------------------------------*/

/*Расписание сеансов*/
CREATE TABLE "CINEMA_RESERVATION"."CINEMA_SCHEDULES"
(
"ID" NUMBER(10,0) NOT NULL ENABLE,
"ID_CINEMA_HALLS" NUMBER(10,0) NOT NULL ENABLE,
"EVENT_NAME" VARCHAR2(1024 CHAR) NOT NULL ENABLE,
"DATE_BEGIN" DATE NOT NULL ENABLE,
"DATE_END" DATE NOT NULL ENABLE,
    CONSTRAINT "CINEMA_SCHEDULES_PK" PRIMARY KEY ("ID"),
    CONSTRAINT "CINEMA_SCHEDULES_ISH_FK" FOREIGN KEY ("ID_CINEMA_HALLS")
        REFERENCES "CINEMA_RESERVATION"."CINEMA_HALLS" ("ID") ON DELETE CASCADE ENABLE,
    CONSTRAINT "CINEMA_SCHEDULES_B_UNIQUE" UNIQUE ("ID_CINEMA_HALLS", "DATE_BEGIN"),
    CONSTRAINT "CINEMA_SCHEDULES_E_UNIQUE" UNIQUE ("ID_CINEMA_HALLS", "DATE_END"),
    CONSTRAINT "DATES_RELATION_CHK" CHECK ("DATE_BEGIN" < "DATE_END")
);

COMMIT;

COMMENT ON TABLE "CINEMA_RESERVATION"."CINEMA_SCHEDULES" IS 'Расписание сеансов в кинозалах кинотеатров';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_SCHEDULES"."ID" IS 'Уникальный идентификатор сеанса';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_SCHEDULES"."ID_CINEMA_HALLS" IS 'Ссылка на идентификатор зала';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_SCHEDULES"."EVENT_NAME" IS 'Наименование проходящего события';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_SCHEDULES"."DATE_BEGIN" IS 'Дата начала события';
COMMENT ON COLUMN "CINEMA_RESERVATION"."CINEMA_SCHEDULES"."DATE_BEGIN" IS 'Дата окончания события';
COMMIT;

Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 1, 'Фильм 1', TO_DATE('2018-12-20 12:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 14:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 1, 'Фильм 1', TO_DATE('2018-12-20 14:30:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 16:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 1, 'Фильм 2', TO_DATE('2018-12-20 10:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 11:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 4, 'Фильм 1', TO_DATE('2018-12-20 10:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 12:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 5, 'Фильм 2', TO_DATE('2018-12-20 16:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 17:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 6, 'Фильм 1', TO_DATE('2018-12-20 20:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 22:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 8, 'Фильм 3', TO_DATE('2018-12-21 12:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-21 14:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 8, 'Фильм 4', TO_DATE('2018-12-22 15:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-22 17:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 9, 'Фильм 1', TO_DATE('2018-12-20 15:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 17:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 12, 'Фильм 1', TO_DATE('2018-12-21 12:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-21 14:00:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 12, 'Фильм 2', TO_DATE('2018-12-22 12:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-22 13:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 12, 'Фильм 3', TO_DATE('2018-12-20 10:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-20 12:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 12, 'Фильм 3', TO_DATE('2018-12-21 16:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-21 18:30:00','SYYYY-MM-DD HH24:MI:SS'));
Insert into "CINEMA_RESERVATION"."CINEMA_SCHEDULES" (ID, ID_CINEMA_HALLS, EVENT_NAME, DATE_BEGIN, DATE_END) values (CINEMA_RESERVATION.CINEMA_SCHEDULES_SEQ.NEXTVAL, 14, 'Фильм 3', TO_DATE('2018-12-22 14:00:00','SYYYY-MM-DD HH24:MI:SS'), TO_DATE('2018-12-22 16:30:00','SYYYY-MM-DD HH24:MI:SS'));
COMMIT;

/*------------------------------------------------*/

/*Бронь на сеансы*/
CREATE TABLE "CINEMA_RESERVATION"."SEATS_RESERVATION"
(
"ID_CINEMA_SCHEDULES" NUMBER(10,0) NOT NULL ENABLE,
"ID_SEATS" NUMBER(10,0) NOT NULL ENABLE,
"DATE_RESERVATION" DATE NOT NULL ENABLE,
"RESERVE_OWNER_NAME" VARCHAR2(1024 CHAR) NOT NULL ENABLE,
"RESERVE_OWNER_PHONE" VARCHAR2(1024 CHAR),
    CONSTRAINT "SEATS_RESERVATION_ICS_FK" FOREIGN KEY ("ID_CINEMA_SCHEDULES")
        REFERENCES "CINEMA_RESERVATION"."CINEMA_SCHEDULES" ("ID") ON DELETE CASCADE ENABLE,
    CONSTRAINT "SEATS_RESERVATION_IS_FK" FOREIGN KEY ("ID_SEATS")
        REFERENCES "CINEMA_RESERVATION"."SEATS" ("ID") ON DELETE CASCADE ENABLE,
    CONSTRAINT "SEATS_RESERVATION_UNIQUE" UNIQUE ("ID_CINEMA_SCHEDULES", "ID_SEATS")
);

COMMIT;

COMMENT ON TABLE "CINEMA_RESERVATION"."SEATS_RESERVATION" IS 'Забронированные места на сеансы';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS_RESERVATION"."ID_CINEMA_SCHEDULES" IS 'Ссылка на идентификатор сеанса';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS_RESERVATION"."ID_SEATS" IS 'Ссылка на идентификатор места';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS_RESERVATION"."DATE_RESERVATION" IS 'Дата и время бронирования';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS_RESERVATION"."RESERVE_OWNER_NAME" IS 'ФИО держателя брони';
COMMENT ON COLUMN "CINEMA_RESERVATION"."SEATS_RESERVATION"."RESERVE_OWNER_NAME" IS 'Номер телефона держателя брони';
COMMIT;

insert into "CINEMA_RESERVATION"."SEATS_RESERVATION" (ID_CINEMA_SCHEDULES, ID_SEATS, DATE_RESERVATION, RESERVE_OWNER_NAME, RESERVE_OWNER_PHONE) values (2, 95, SYSDATE, 'Человек 1', null);
insert into "CINEMA_RESERVATION"."SEATS_RESERVATION" (ID_CINEMA_SCHEDULES, ID_SEATS, DATE_RESERVATION, RESERVE_OWNER_NAME, RESERVE_OWNER_PHONE) values (2, 83, SYSDATE, 'Человек 2', '8-999-111-22-33');
COMMIT;