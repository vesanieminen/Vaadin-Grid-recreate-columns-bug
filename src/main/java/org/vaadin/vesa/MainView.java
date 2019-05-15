package org.vaadin.vesa;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    private MyGrid grid;
    private Select<String>[] selects;

    private final List<String> headers = Arrays.asList("firstName", "lastName", "age", "gender", "nickname");
    private final Map<String, ValueProvider<Person, ?>> headerToValueProvider = new HashMap<String, ValueProvider<Person, ?>>() {{
        put("firstName", person -> person.firstName);
        put("lastName",  person -> person.lastName);
        put("age",  person -> person.age);
        put("gender",  person -> person.gender);
        put("nickname",  person -> person.nickname);
    }};
    private final Person[] people = new Person[] {
            new Person("Ben", "Hanks", 25, "Male", "Bensy"),
            new Person("Zackhary", "Smith", 26, "Male", "Zacsy"),
            new Person("Tom", "Jones", 27, "Male", "Tomsy"),
            new Person("Jerry", "Stallone", 28, "Male", "Jersey"),
            new Person("Bob", "Rourke", 29, "Male", "Bobsy")
    };

    class Person {
        String firstName;
        String lastName;
        Integer age;
        String gender;
        String nickname;
        Person(String firstName, String lastName, Integer age, String gender, String nickname) {
            this.firstName = firstName; this.lastName = lastName; this.age = age; this.gender = gender; this.nickname = nickname;
        }
    }

    class MyGrid extends Grid<Person> {

        private Function<Integer, Component> headerGenerator;
        private HeaderRow headerRow;

        public void setHeaderGenerator(Function<Integer, Component> headerGenerator) {
            headerRow = addFirstHeaderRow();
            this.headerGenerator = headerGenerator;
            redraw();
        }

        void setData() {
            setItems(people);
        }

        public void redraw() {
            setData();
            addColumnsToContainer();
            getDataCommunicator().reset();
        }

        void addColumnsToContainer() {
            removeAllColumns();
            for (int i = 0; i < 5; ++i) {
                String value = selects[i].getValue();
                addColumn(headerToValueProvider.get(value));
                headerRow.getCells().get(i).setComponent(headerGenerator.apply(i));
                getElement().executeJavaScript("$0.$server.enableSorting();", getElement());
            }
        }

        @ClientCallable
        private void enableSorting() {
            getColumns().forEach(column -> column.setSortable(true));
        }

    }

    public Function<Integer, Component> getHeaderGenerator() {
        return columnIndex -> selects[columnIndex];
    }

    public MainView() {
        selects = new Select[5];
        for (int i = 0; i < 5; ++i) {
            Select<String> select = new Select<>();
            select.setItems(headers);
            select.setValue(headers.get(i));
            select.getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
            int index = i;
            select.addValueChangeListener(e -> {
                grid.sort(null);
                grid.redraw();
                grid.getElement().executeJavaScript("$0.$server.enableSorting();", grid.getElement());
            });
            selects[i] = select;

        }
        grid = new MyGrid();
        grid.setHeaderGenerator(getHeaderGenerator());

        add(grid);
        grid.redraw();
    }


}
