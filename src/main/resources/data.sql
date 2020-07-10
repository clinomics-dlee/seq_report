INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_GUEST_00' AS code, '방문자', 0, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_GUEST_00');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_INPUT_20' AS code, '입고담당자', 1, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_INPUT_20');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_INPUT_40' AS code, '입고관리자', 1, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_INPUT_40');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_OUTPUT_20' AS code, '출고담당자', 1, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_OUTPUT_20');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_EXP_20' AS code, '검사담당자', 0, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_EXP_20');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_EXP_40' AS code, '검사실관리자', 0, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_EXP_40');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_EXP_80' AS code, '검사실책임자', 1, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_EXP_80');
INSERT INTO role (code, name, is_personal_view, in_use)
SELECT 'ROLE_IT_99' AS code, 'IT관리자', 1, 1 AS name FROM DUAL WHERE NOT EXISTS (SELECT * FROM role WHERE code = 'ROLE_IT_99');
INSERT INTO member (id, dept, email, is_failed_mail_sent, in_use, name, password)
SELECT 'admin', 'IT Infra', 'info@clinomics.com', 1, 1, 'Administrator', '$10$8y7E2JJg7d68OQSFKw5rmePUCEd5NtyCKhoX5Ue.0n46veUaHw6Oq' FROM DUAL WHERE NOT EXISTS (SELECT * FROM member WHERE id = 'admin');
INSERT INTO member_role (member_id, role_id)
SELECT 'admin', id FROM role WHERE code = 'IT_99' AND NOT EXISTS (SELECT * FROM member WHERE id = 'admin');