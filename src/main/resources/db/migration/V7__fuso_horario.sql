UPDATE revisao SET data = (data AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';
UPDATE acesso SET data = (data AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';
UPDATE importacaoanvisa SET importado_em = (importado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';
UPDATE importacaoanvisa_aud SET importado_em = (importado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';
