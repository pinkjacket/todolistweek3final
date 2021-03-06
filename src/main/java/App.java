

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.Mod;
import dao.Sql2oCategoryDao;
import dao.Sql2oTaskDao;
import models.Category;
import models.Task;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import static spark.Spark.*;

public class App {

    public static void main(String[] args) {
        staticFileLocation("/public");
        String connectionString = "jdbc:h2:~/todolist.db;INIT=RUNSCRIPT from 'classpath:db/create.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        Sql2oTaskDao taskDao = new Sql2oTaskDao(sql2o);
        Sql2oCategoryDao categoryDao = new Sql2oCategoryDao(sql2o);



        //get: show a specific task in a specific category
        // /categories/:category_id/tasks/:task_id

        //get: show a form to create a new category
         get("/categories/new", (request, response) -> {
             Map<String, Object> model = new HashMap<>();

             List<Category> categories = categoryDao.getAll();
             model.put("categories", categories);

             return new ModelAndView(model, "category-form.hbs");
         }, new HandlebarsTemplateEngine());



        //post: process a form to create a new category
         post("/categories", (request, response) -> {
             Map<String, Object> model = new HashMap<>();
             String name = request.queryParams("name");
             Category newCategory = new Category(name);
             categoryDao.add(newCategory);

             List<Category> categories = categoryDao.getAll();
             model.put("categories", categories);

             return new ModelAndView(model, "success.hbs");
         }, new HandlebarsTemplateEngine());

        //get: show a form to update a category
         get("/categories/update", (request, response) -> {
             Map<String, Object> model = new HashMap<>();

             model.put("editCategory", true);

             List<Category> allCategories = categoryDao.getAll();
             model.put("categories", allCategories);

             return new ModelAndView(model, "category-form.hbs");
         }, new HandlebarsTemplateEngine());

         //post: process category update form
        post("/categories/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfCategoryToEdit = Integer.parseInt(request.queryParams("editCategoryId"));
            String newName = request.queryParams("newCategoryName");
            categoryDao.update(categoryDao.findById(idOfCategoryToEdit).getId(), newName);

            List<Category> categories = categoryDao.getAll();
            model.put("categories", categories);

            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: delete a category AND all tasks it contains
        // /categories/:category_id/delete

        //get: delete all categories and all tasks
        get("/categories/delete", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            taskDao.clearAllTasks();
            categoryDao.clearAllCategories();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show all tasks in all categories and show all categories
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            List<Task> tasks = taskDao.getAll();
            model.put("tasks", tasks);
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        //get: delete all tasks
        get("/tasks/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            taskDao.clearAllTasks();
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show new task form
        get("/tasks/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            return new ModelAndView(model, "task-form.hbs");
        }, new HandlebarsTemplateEngine());

        //post: process new task form
        post("/tasks/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);
            String description = request.queryParams("description");
            int newCategoryId = Integer.parseInt(request.queryParams("categoryId"));
            Task newTask = new Task(description, newCategoryId); //ignore the hardcoded categoryId
            taskDao.add(newTask);
            model.put("task", newTask);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show a form to update a task
        get("/tasks/update", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            List<Task> allTasks = taskDao.getAll();
            model.put("tasks", allTasks);

            model.put("editTask", true);
            return new ModelAndView(model, "task-form.hbs");
        }, new HandlebarsTemplateEngine());

//        //post: process a form to update a task
//        post("/tasks/update", (req, res) -> {
//            Map<String, Object> model = new HashMap<>();
//
//            List<Category> allCategories = categoryDao.getAll();
//            model.put("categories", allCategories);
//
//            String newContent = req.queryParams("description");
//            int newCategoryId = Integer.parseInt(req.queryParams("categoryId"));
//            int idOfTaskToEdit = Integer.parseInt(req.queryParams("id"));
//            Task editTask = taskDao.findById(idOfTaskToEdit);
//            taskDao.update(idOfTaskToEdit, newContent, newCategoryId); //ignore the hardcoded categoryId for now.
//            return new ModelAndView(model, "success.hbs");
//        }, new HandlebarsTemplateEngine());

        //post: process a form to update a task
        post("/tasks/update", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            String newContent = req.queryParams("description");
            int newCategoryId = Integer.parseInt(req.queryParams("categoryId"));
            int taskToEditId = Integer.parseInt(req.queryParams("taskToEditId"));
            Task editTask = taskDao.findById(taskToEditId);
            taskDao.update(taskToEditId, newContent, newCategoryId);

            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show an individual task that is nested in a category
        get("/categories/:category_id/tasks/:task_id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfTaskToFind = Integer.parseInt(req.params("task_id"));
            Task foundTask = taskDao.findById(idOfTaskToFind);
            model.put("task", foundTask);
            return new ModelAndView(model, "task-detail.hbs");
        }, new HandlebarsTemplateEngine());

        //get a specific category (and the tasks it contains)
        get("/categories/:category_id", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfCategoryToFind = Integer.parseInt(request.params("category_id"));

            List<Category> categories = categoryDao.getAll();
            model.put("categories", categories);

            Category foundCategory = categoryDao.findById(idOfCategoryToFind);
            model.put("category", foundCategory);
            List<Task> allTasksByCategory = categoryDao.getAllTasksByCategory(idOfCategoryToFind);
            model.put("tasks", allTasksByCategory);

            return new ModelAndView(model, "category-detail.hbs");
        }, new HandlebarsTemplateEngine());

        //get: delete an individual task
        get("categories/:category_id/tasks/:task_id/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfTaskToDelete = Integer.parseInt(req.params("task_id"));
            Task deleteTask = taskDao.findById(idOfTaskToDelete);
            taskDao.deleteById(idOfTaskToDelete);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());
    }
}
