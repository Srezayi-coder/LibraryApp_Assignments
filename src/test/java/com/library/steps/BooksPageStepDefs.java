package com.library.steps;

import com.library.pages.BasePage;
import com.library.pages.BookPage;
import com.library.pages.BorrowedBooksPage;
import com.library.pages.DashBoardPage;
import com.library.utility.BrowserUtil;
import com.library.utility.DB_Util;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Map;

public class BooksPageStepDefs {

    BasePage basePage;
    List<String> categoriesFromUI;
    BookPage bookPage = new BookPage();
    @When("the user navigates to {string} page")
    public void the_user_navigates_to_page(String module) {

        basePage=new BookPage();
        basePage.navigateModule(module);


    }
    @When("the user clicks book categories")
    public void the_user_clicks_book_categories() {

        Select select=new Select(((BookPage)basePage).mainCategoryElement);


        List<WebElement> options = select.getOptions();
         categoriesFromUI= BrowserUtil.getElementsText(options);

         categoriesFromUI.remove("ALL");
        System.out.println("categoriesFromUI = " + categoriesFromUI);

    }
    @Then("verify book categories must match book_categories table from db")
    public void verify_book_categories_must_match_book_categories_table_from_db() {

        DB_Util.runQuery("select name from book_categories");
        List<String> categoriesFromDB = DB_Util.getColumnDataAsList("name");
        System.out.println("categoriesFromDB = " + categoriesFromDB);

        Assert.assertEquals(categoriesFromUI,categoriesFromDB);

    }

    String searchedBook; // more accessible variable that will hold book name
    @When("the user searches for {string} book")
    public void theUserSearchesForBook(String bookName) {
        // bookName --> local to the method itself
        searchedBook = bookName;
        bookPage.search.sendKeys(searchedBook);
        BrowserUtil.waitFor(1);
    }

    @And("the user clicks edit book button")
    public void theUserClicksEditBookButton() {
        bookPage.editBook(searchedBook).click();
        BrowserUtil.waitFor(1);
    }

    @Then("book information must match the Database")
    public void bookInformationMustMatchTheDatabase() {
        // 1. Get data from UI elements
        String UIbookName = bookPage.bookName.getAttribute("value");
        System.out.println("UIbookName = " + UIbookName);
        String UIauthorName = bookPage.author.getAttribute("value");
        String UI_ISBN = bookPage.isbn.getAttribute("value");
        String UIdescription = bookPage.description.getAttribute("value");
        String UI_year = bookPage.year.getAttribute("value");

        Select categoryDropDown = new Select(bookPage.categoryDropdown);
        String UI_category = categoryDropDown.getFirstSelectedOption().getText();

        // 2.Get data from DB
        String query = "select b.name as bookName, b.year, b.author, b.description, b.isbn, bc.name as categoryName from books b inner join book_categories bc on b.book_category_id = bc.id where b.name='"+searchedBook+"'";
        DB_Util.runQuery(query);
        Map<String, String> DB_info_rowMap = DB_Util.getRowMap(1);
        System.out.println("DB_info_rowMap = " + DB_info_rowMap);


        // 3. Compare 2 data
        Assert.assertEquals(UIbookName,DB_info_rowMap.get("bookName"));
        Assert.assertEquals(UI_category,DB_info_rowMap.get("categoryName"));
        Assert.assertEquals(UIdescription,DB_info_rowMap.get("description"));




    }

    String DB_mostPopularGenre;
    @When("I execute query to find most popular book genre")
    public void iExecuteQueryToFindMostPopularBookGenre() {

    String query = "select bc.name, count(*) from book_borrow bb\n" +
            "        inner join books b on bb.book_id = b.id\n" +
            "        inner join book_categories bc on b.book_category_id = bc.id\n" +
            "        group by bc.name order by count(*) desc limit 1";
    DB_Util.runQuery(query);
    DB_mostPopularGenre = DB_Util.getFirstRowFirstColumn();
        System.out.println("DB_mostPopularGenre = " + DB_mostPopularGenre);
    }

    @Then("verify {string} is the most popular book genre.")
    public void verifyIsTheMostPopularBookGenre(String expectedGenre) {
        Assert.assertEquals(expectedGenre,DB_mostPopularGenre);
    }

    // US 06
    @When("the librarian click to add book")
    public void the_librarian_click_to_add_book() {
        bookPage.addBook.click();
    }
    @When("the librarian enter book name {string}")
    public void the_librarian_enter_book_name(String name) {
        bookPage.bookName.sendKeys(name);
    }
    @When("the librarian enter ISBN {string}")
    public void the_librarian_enter_isbn(String isbn) {
        bookPage.isbn.sendKeys(isbn);

    }
    @When("the librarian enter year {string}")
    public void the_librarian_enter_year(String year) {
        bookPage.year.sendKeys(year);
    }
    @When("the librarian enter author {string}")
    public void the_librarian_enter_author(String author) {
        bookPage.author.sendKeys(author);
    }
    @When("the librarian choose the book category {string}")
    public void the_librarian_choose_the_book_category(String category) {
        BrowserUtil.selectOptionDropdown(bookPage.categoryDropdown,category);
    }
    @When("the librarian click to save changes")
    public void the_librarian_click_to_save_changes() {
        bookPage.saveChanges.click();
    }


    @Then("verify {string} message is displayed")
    public void verify_the_book_has_been_created_message_is_displayed(String expectedMessage) {
        // You can verify message itself too both works
        //OPT 1
        String actualMessage = bookPage.toastMessage.getText();
        Assert.assertEquals(expectedMessage,actualMessage);

        //OPT 2
        Assert.assertTrue(bookPage.toastMessage.isDisplayed());

    }

    @Then("verify {string} information must match with DB")
    public void verify_information_must_match_with_db(String expectedBookName) {
        String query = "select name, author, isbn from books\n" +
                "where name = '"+expectedBookName+"'";

        DB_Util.runQuery(query);

        Map<String, String> rowMap = DB_Util.getRowMap(1);

        String actualBookName = rowMap.get("name");

        Assert.assertEquals(expectedBookName,actualBookName);

    }


    // US 07

    @When("the user clicks Borrow Book")
    public void the_user_clicks_borrow_book() {
        bookPage.borrowBook(searchedBook).click();
        BrowserUtil.waitFor(2);
    }

    @Then("verify that book is shown in {string} page")
    public void verify_that_book_is_shown_in_page(String module) {
        BorrowedBooksPage borrowedBooksPage = new BorrowedBooksPage();
        new DashBoardPage().navigateModule(module);

        List<String> allBorrowedBooks = BrowserUtil.getElementsText(borrowedBooksPage.allBorrowedBooksName);
        boolean result = allBorrowedBooks.contains(searchedBook);
        Assert.assertTrue(result);
    }
    @Then("verify logged student has same book in database")
    public void verify_logged_student_has_same_book_in_database() {
        String query = "select name from books b\n" +
                "join book_borrow bb on b.id = bb.book_id\n" +
                "join users u on bb.user_id = u.id\n" +
                "where name = '"+searchedBook+"' and full_name = 'Test Student 5';";

        DB_Util.runQuery(query);
        List<String> actualList = DB_Util.getColumnDataAsList(1);
        Assert.assertTrue(actualList.contains(searchedBook));
    }

}
