CREATE TABLE company (
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

insert into company (id, name) values
(1, 'Blueberries'),
(2, 'Yandex'),
(3, 'Dell'),
(4, 'Canon'),
(5, 'Samsung'),
(6, 'Yaguar');

select  * from company;

insert into person (id, name, company_id) values (1, 'Rob', 1),(4, 'Bob', 1),
(2, 'Greg', 2), (3, 'Jim', 6), (11, 'Jeffrey', 6), (6, 'Jacob', 6), (7, 'James', 4), (10, 'Charles', 5), (8, '', 2),
(5, 'Ari', 6), (12, 'Taron', null);

select p.name as person_name, c.name as work_place_name
from person as p left join company as c 
on p.company_id = c.id where p.company_id != 5 or p.company_id is null order by p.name ASC;

select c.name as work_place_name, count(p.company_id) 
from company as c join person as p 
on c.id = p.company_id
group by c.name
having count(p.company_id) = (
    select count(company_id) as c from person
    group by company_id
    order by c DESC
    limit 1
);
