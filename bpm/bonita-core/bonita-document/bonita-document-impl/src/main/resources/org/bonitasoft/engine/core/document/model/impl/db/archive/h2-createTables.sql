CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);