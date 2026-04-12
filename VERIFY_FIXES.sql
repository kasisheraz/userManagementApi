-- =====================================================
-- VERIFICATION SCRIPT
-- Run these queries to confirm the fixes are in place
-- =====================================================

-- CHECK 1: Verify all three roles exist
SELECT 'Check 1: USER, MANAGER, SUPER_ADMIN roles exist?' as Check_Name,
       CASE 
           WHEN COUNT(*) = 3 THEN '✅ PASS - All roles found'
           ELSE '❌ FAIL - Missing roles'
       END as Result,
       COUNT(*) as Roles_Found
FROM Roles
WHERE Role_Name IN ('USER', 'MANAGER', 'SUPER_ADMIN');

-- CHECK 2: List all roles
SELECT 'Check 2: All available roles:' as Info,
       Role_Identifier, 
       Role_Name, 
       Role_Description
FROM Roles
ORDER BY Role_Identifier;

-- CHECK 3: Verify USER role has permissions
SELECT 'Check 3: USER role permissions:' as Info,
       COUNT(*) as Permission_Count
FROM Roles r
JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
WHERE r.Role_Name = 'USER';

-- CHECK 4: Show USER role permission details
SELECT 'Check 4: USER role permission details:' as Info,
       r.Role_Name,
       p.Permission_Name,
       p.Description
FROM Roles r
JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
JOIN Permissions p ON rp.Permission_Identifier = p.Permission_Identifier
WHERE r.Role_Name = 'USER'
ORDER BY p.Permission_Name;

-- CHECK 5: Verify no organizations have empty company numbers
SELECT 'Check 5: Organizations with empty/null company numbers?' as Check_Name,
       CASE 
           WHEN COUNT(*) = 0 THEN '✅ PASS - No empty strings found'
           ELSE CONCAT('❌ FAIL - Found ', COUNT(*), ' empty strings')
       END as Result
FROM Organisation
WHERE Company_Number = '';

-- CHECK 6: Show organizations with NULL company numbers (this is OK)
SELECT 'Check 6: Organizations with NULL company numbers (allowed):' as Info,
       COUNT(*) as Count
FROM Organisation
WHERE Company_Number IS NULL;

-- CHECK 7: List all organizations
SELECT 'Check 7: All organizations:' as Info,
       Organisation_Identifier,
       Legal_Name,
       COALESCE(Company_Number, 'NULL') as Company_Number,
       COALESCE(Registration_Number, 'NULL') as Registration_Number,
       Organisation_Type
FROM Organisation
ORDER BY Organisation_Identifier;

-- =====================================================
-- FINAL SUMMARY
-- =====================================================
SELECT '========================================' as Summary;
SELECT 'VERIFICATION COMPLETE' as Summary;
SELECT '========================================' as Summary;

SELECT 'Expected Results:' as Summary;
SELECT '✅ 3 roles found (USER, MANAGER, SUPER_ADMIN)' as Summary;
SELECT '✅ USER role has at least 2 permissions' as Summary;
SELECT '✅ No empty string company numbers' as Summary;
SELECT '✅ NULL company numbers are OK' as Summary;
