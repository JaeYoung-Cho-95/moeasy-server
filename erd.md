erDiagram
MEMBER ||--o{ QUESTION: member_id
QUESTION ||--|| SURVEY: survey_id

    MEMBER {
        BIGINT id PK
        VARCHAR username
        VARCHAR email
        VARCHAR profileUrl
    }
    
    QUESTION {
        BIGINT id PK
        BIGINT member_id FK
        BIGINT survey_id FK
        VARCHAR title
        JSON content
        DATETIME createdTime
        DATETIME expiredTime
        BOOLEAN expired
        INT count
        VARCHAR urlInQrCode
    }
    
    SURVEY {
        BIGINT id PK
        JSON resultsJson
        JSON summarizeJson
        DATETIME lastUpdated
    }
    
    REFRESH_TOKEN {
        BIGINT id PK
        VARCHAR userEmail
        VARCHAR token
    }
