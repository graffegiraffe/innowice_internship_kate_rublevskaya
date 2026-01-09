CREATE SCHEMA IF NOT EXISTS orderservice;
SET search_path TO orderservice;

GRANT ALL PRIVILEGES ON SCHEMA orderservice TO katusha;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA orderservice TO katusha;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA orderservice TO katusha;