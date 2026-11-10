-- ADMIN passa a poder abrir chamados.
INSERT INTO perfil_authority (perfil_id, authority_id)
SELECT p.id, a.id
FROM perfil p
CROSS JOIN authority a
WHERE p.codigo = 'ADMIN'
  AND a.codigo = 'CHAMADO_CRIAR'
  AND NOT EXISTS (
      SELECT 1
      FROM perfil_authority pa
      WHERE pa.perfil_id = p.id
        AND pa.authority_id = a.id
  );
