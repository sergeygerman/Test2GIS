--select 'select * from ' || OBJECT_NAME ||';' from ALL_OBJECTS where OWNER = 'CINEMA_RESERVATION' and OBJECT_TYPE='TABLE';
--select 'drop table ' || OWNER || '.' || OBJECT_NAME ||' cascade constraints;' from ALL_OBJECTS where OWNER = 'CINEMA_RESERVATION' and OBJECT_TYPE='TABLE';
--select 'drop sequence ' || OWNER || '.' || OBJECT_NAME ||';' from ALL_OBJECTS where OWNER = 'CINEMA_RESERVATION' and OBJECT_TYPE='SEQUENCE';

drop table CINEMA_RESERVATION.SEATS_RESERVATION cascade constraints;
drop table CINEMA_RESERVATION.CINEMA_SCHEDULES cascade constraints;
drop table CINEMA_RESERVATION.SEATS cascade constraints;
drop table CINEMA_RESERVATION.CINEMA_HALLS cascade constraints;
drop table CINEMA_RESERVATION.CINEMAS cascade constraints;
COMMIT;

drop sequence "CINEMA_RESERVATION"."CINEMA_SCHEDULES_SEQ";
drop sequence "CINEMA_RESERVATION"."SEATS_RESERVATION_SEQ";
COMMIT;