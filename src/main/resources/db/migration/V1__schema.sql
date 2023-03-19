CREATE TABLE scalar_question
(
    id       BIGINT    NOT NULL AUTO_INCREMENT,
    question CHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE scalar_answer
(
    id                 BIGINT    NOT NULL AUTO_INCREMENT,
    scalar             SMALLINT  NOT NULL,
    author_id          CHAR(255) NOT NULL,
    stats_calculated   BOOL      NOT NULL DEFAULT false,
    scalar_question_id BIGINT    NOT NULL REFERENCES scalar_question (id),
    PRIMARY KEY (id),
    INDEX (scalar_question_id, stats_calculated)
);

CREATE TABLE scalar_answer_stats_per_answer
(
    id                 BIGINT   NOT NULL AUTO_INCREMENT,
    count              BIGINT   NOT NULL,
    answer             SMALLINT NOT NULL,
    scalar_question_id BIGINT   NOT NULL REFERENCES scalar_question (id),
    PRIMARY KEY (id),
    UNIQUE INDEX (scalar_question_id, answer)
);

CREATE TABLE scalar_answer_stats
(
    count              BIGINT NOT NULL,
    sum                BIGINT NOT NULL,
    average            BIGINT NOT NULL,
    scalar_question_id BIGINT NOT NULL REFERENCES scalar_question (id),
    PRIMARY KEY (scalar_question_id)
);

CREATE TABLE text_question
(
    id       BIGINT    NOT NULL AUTO_INCREMENT,
    question CHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE text_answer
(
    id               BIGINT    NOT NULL AUTO_INCREMENT,
    text             CHAR(255) NOT NULL,
    author_id        CHAR(255) NOT NULL,
    stats_calculated BOOL      NOT NULL DEFAULT false,
    text_question_id BIGINT    NOT NULL REFERENCES text_question (id),
    PRIMARY KEY (id),
    INDEX (text_question_id, stats_calculated)
);

CREATE TABLE text_answer_word
(
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    word             CHAR(48) NOT NULL,
    count            BIGINT   NOT NULL,
    text_question_id BIGINT   NOT NULL REFERENCES text_question (id),
    PRIMARY KEY (id),
    UNIQUE INDEX (text_question_id, word),
    INDEX (text_question_id)
);

CREATE TABLE text_answer_stats
(
    count            BIGINT NOT NULL,
    text_question_id BIGINT NOT NULL REFERENCES text_question (id),
    PRIMARY KEY (text_question_id)
);
