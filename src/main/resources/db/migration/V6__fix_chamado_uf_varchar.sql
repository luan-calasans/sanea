-- Hibernate validate espera VARCHAR; PostgreSQL CHAR vira bpchar.
ALTER TABLE chamado
    ALTER COLUMN uf TYPE VARCHAR(2)
    USING trim(uf);
