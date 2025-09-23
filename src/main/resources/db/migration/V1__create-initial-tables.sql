-- V1__create-initial-tables.sql

-- Tabela de Organizações (para o modelo SaaS)
CREATE TABLE organizations (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(150) NOT NULL UNIQUE,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabelas de domínio (lookup tables)
CREATE TABLE roles (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(20) NOT NULL UNIQUE -- "ROLE_USER", "ROLE_TECH", "ROLE_ADMIN"
);

CREATE TABLE priorities (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(20) NOT NULL UNIQUE -- "Baixa", "Média", "Alta"
);

CREATE TABLE statuses (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE -- "Aberto", "Em Andamento", "Fechado"
);

-- Tabela de Usuários (agora ligada a uma organização)
CREATE TABLE users (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id),
  name            VARCHAR(100) NOT NULL,
  email           VARCHAR(100) NOT NULL,
  password        VARCHAR(255) NOT NULL,
  role_id         INT NOT NULL REFERENCES roles(id),
  created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(organization_id, email) -- Email deve ser único por organização
);

-- Tabela de Chamados (Tickets)
CREATE TABLE tickets (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id),
  title           VARCHAR(150) NOT NULL,
  description     TEXT NOT NULL,
  priority_id     INT NOT NULL REFERENCES priorities(id),
  status_id       INT NOT NULL REFERENCES statuses(id),
  created_by      BIGINT NOT NULL REFERENCES users(id),
  assigned_to     BIGINT REFERENCES users(id), -- Pode ser nulo
  created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Auditoria dos Chamados
CREATE TABLE ticket_audits (
  id           BIGSERIAL PRIMARY KEY,
  ticket_id    BIGINT NOT NULL REFERENCES tickets(id),
  field_name   VARCHAR(50) NOT NULL,
  old_value    TEXT,
  new_value    TEXT,
  changed_by   BIGINT NOT NULL REFERENCES users(id),
  changed_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Adicionando alguns dados iniciais para as tabelas de domínio
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_TECH'), ('ROLE_ADMIN');
INSERT INTO priorities (name) VALUES ('Baixa'), ('Média'), ('Alta');
INSERT INTO statuses (name) VALUES ('Aberto'), ('Em Andamento'), ('Pendente'), ('Fechado');