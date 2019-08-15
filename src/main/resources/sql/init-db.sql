CREATE TABLE snippets (
  id         INTEGER PRIMARY KEY,
  time     TIMESTAMP,
  username VARCHAR(30),
  exercise VARCHAR(1024),
  snippet  VARCHAR(10000),
  status    VARCHAR(30),
  completion DOUBLE,
  reset    BOOLEAN
);
