
CREATE TABLE IF NOT EXISTS transfers
(
   id             VARCHAR(50)     PRIMARY KEY NOT NULL                                ,
   from_position  VARCHAR(50)                 NOT NULL                                ,
   to_position    VARCHAR(50)                 NOT NULL                                ,
   initiated_at   DATE                        Not NULL                                ,
   finished_at    DATE                                                                ,
   amount         DOUBLE                                                              ,
   status         VARCHAR(10)                                                         ,
   comments       VARCHAR(255)
);