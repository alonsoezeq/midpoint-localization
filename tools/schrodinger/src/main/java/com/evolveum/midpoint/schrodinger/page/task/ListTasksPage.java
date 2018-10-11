package com.evolveum.midpoint.schrodinger.page.task;

import com.codeborne.selenide.SelenideElement;
import com.evolveum.midpoint.schrodinger.component.common.FeedbackBox;
import com.evolveum.midpoint.schrodinger.component.task.TasksPageTable;
import com.evolveum.midpoint.schrodinger.page.BasicPage;
import com.evolveum.midpoint.schrodinger.util.Schrodinger;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ListTasksPage extends BasicPage {

    public TasksPageTable<ListTasksPage> table() {
        SelenideElement box = $(Schrodinger.byDataId("div", "taskTable"));

        return new TasksPageTable<>(this, box);
    }

}
