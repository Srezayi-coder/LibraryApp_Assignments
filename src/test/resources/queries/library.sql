select * from users;

-- US01-1
   -- OPT 1
    select count(id) from users;           -- 138  -- ACTUAL
    select  count(distinct id) from users; -- 138  -- EXPECTED

   -- OPT 2
    select id from users;
    -- getAllColumnAsList --> List --> size  --> EXPECTED
    -- getAllColumnAsList --> Set  --> size  --> EXPECTED

    select * from users;


select count(*) from book_borrow where is_returned=0;

select name from book_categories;

select b.name, b.year, b.author, b.description, b.isbn, bc.name from books b inner join book_categories bc on b.book_category_id = bc.id where b.name='Clean Code';
