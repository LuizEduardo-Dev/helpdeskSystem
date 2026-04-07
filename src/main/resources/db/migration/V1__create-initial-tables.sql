-- 1. Organizações
CREATE TABLE organizations (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(150) NOT NULL UNIQUE,
  is_active  BOOLEAN NOT NULL DEFAULT TRUE, -- Soft delete/Suspensão por falta de pagamento
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabelas de domínio (Mantidas globais para a V1)
CREATE TABLE roles (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE priorities (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE statuses (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Usuários
CREATE TABLE users (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id),
  name            VARCHAR(100) NOT NULL,
  email           VARCHAR(100) NOT NULL,
  password        VARCHAR(255) NOT NULL,
  role_id         INT NOT NULL REFERENCES roles(id),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE, -- Soft delete para não perder histórico
  created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

  -- UNIQUE constraint original mantida, pois um email só existe uma vez por tenant
  UNIQUE(organization_id, email),

  -- Constraint única para permitir FKs compostas mais seguras abaixo
  UNIQUE(id, organization_id)
);

-- 4. Chamados (Tickets)
CREATE TABLE tickets (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id),
  title           VARCHAR(150) NOT NULL,
  description     TEXT NOT NULL,
  priority_id     INT NOT NULL REFERENCES priorities(id),
  status_id       INT NOT NULL REFERENCES statuses(id),
  version         BIGINT NOT NULL DEFAULT 0, -- Para controle de concorrência otimista
  -- FK Composta. Garante que o criador pertence à MESMA organização do ticket
  created_by      BIGINT NOT NULL,
  FOREIGN KEY (created_by, organization_id) REFERENCES users(id, organization_id),

  -- FK Composta. Garante que o técnico designado pertence à MESMA organização do ticket
  assigned_to     BIGINT,
  FOREIGN KEY (assigned_to, organization_id) REFERENCES users(id, organization_id),

  created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Auditoria dos Chamados
CREATE TABLE ticket_audits (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id), -- Denormalização para Multi-tenant
  ticket_id       BIGINT NOT NULL REFERENCES tickets(id),
  field_name      VARCHAR(50) NOT NULL,
  old_value       TEXT,
  new_value       TEXT,
  changed_by      BIGINT NOT NULL REFERENCES users(id),
  created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP -- Padronizado (substitui changed_at da sua V1 e V2)
);

-- 6. Comentários (Ticket Comments)
CREATE TABLE ticket_comments (
    id              BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id), -- Denormalização para Multi-tenant
    ticket_id       BIGINT NOT NULL REFERENCES tickets(id),

    -- FK Composta. Garante que quem comenta pertence à organização do ticket
    user_id         BIGINT NOT NULL,
    FOREIGN KEY (user_id, organization_id) REFERENCES users(id, organization_id),

    content         TEXT NOT NULL,
    is_internal     BOOLEAN NOT NULL DEFAULT FALSE, -- Permite notas privadas entre técnicos
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Inserts Iniciais
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_TECH'), ('ROLE_ADMIN');
INSERT INTO priorities (name) VALUES ('Baixa'), ('Média'), ('Alta');
INSERT INTO statuses (name) VALUES ('Aberto'), ('Em Andamento'), ('Pendente'), ('Fechado');

-- ==============================================================================
-- CRIAÇÃO DE ÍNDICES PARA PERFORMANCE (B-Trees)
-- ==============================================================================

-- Índices para isolamento Multi-tenant (Consultas mais frequentes do SaaS)
CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_tickets_organization_id ON tickets(organization_id);
CREATE INDEX idx_ticket_comments_organization_id ON ticket_comments(organization_id);
CREATE INDEX idx_ticket_audits_organization_id ON ticket_audits(organization_id);

-- Índices para relacionamentos e filtros do HelpDesk
CREATE INDEX idx_tickets_status_id ON tickets(status_id); -- Filtro de "Chamados Abertos"
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to); -- Filtro de "Meus Chamados"
CREATE INDEX idx_tickets_created_by ON tickets(created_by); -- Filtro do cliente

CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments(ticket_id);
CREATE INDEX idx_ticket_audits_ticket_id ON ticket_audits(ticket_id);

-- Opcional, mas recomendado: Índice para o email na hora do login
CREATE INDEX idx_users_email ON users(email);