1. Technology stack:
   a. Springboot 4.0


Table Users {

User_Identifier INT [PRIMARY KEY]

Phone_Number Varchar(20) Unique

Email Varchar(50)

Role_Identifier INT

First_Name Varchar(100)

Middle_Name Varchar(100)

Last_Name Varchar(100)

Date_Of_Birth Date

Residential_Address_Identifier INT

Postal_Address_Identifier INT

Status_Description Varchar(20)

Created_Datetime TIMESTAMP

Last_Modified_Datetime TIMESTAMP

}

Table Roles {

Role_Identifier INT [PRIMARY KEY]

Role_Name Varchar(30)

Role_Description Varchar(100)

Created_Datetime TIMESTAMP

}

TABLE Permissions {

    Permission_Identifier INT [PRIMARY KEY]

    Permission_Name VARCHAR(100)

    Description TEXT

    Resource VARCHAR(50)

    Action VARCHAR(50)

    Created_at TIMESTAMP

}

TABLE Role_Permissions {

    Role_Identifier INT [Primary Key]

    Permission_Identifier INT [Primary Key]

    Granted_At TIMESTAMP

}

Ref: "Roles"."Role_Identifier" < "Role_Permissions"."Role_Identifier"

Ref: "Permissions"."Permission_Identifier" < "Role_Permissions"."Permission_Identifier"

Ref: "Roles"."Role_Identifier" < "Users"."Role_Identifier"
