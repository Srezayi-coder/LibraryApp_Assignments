package com.library.steps;

import com.library.pages.BasePage;
import com.library.pages.BookPage;
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
}
