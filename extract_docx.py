import sys
sys.path.insert(0, r'C:\Users\shera\AppData\Local\Packages\PythonSoftwareFoundation.Python.3.9_qbz5n2kfra8p0\LocalCache\local-packages\Python39\site-packages')

from docx import Document

doc = Document(r'c:\Development\git\userManagementApi\FinCore Platform-UserRequirements-2.docx')

with open(r'c:\Development\git\userManagementApi\extracted_content.txt', 'w', encoding='utf-8') as f:
    for para in doc.paragraphs:
        f.write(para.text + '\n')
