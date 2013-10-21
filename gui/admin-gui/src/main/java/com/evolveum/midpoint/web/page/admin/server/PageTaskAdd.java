/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.admin.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.web.page.admin.server.dto.*;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.evolveum.midpoint.common.security.MidPointPrincipal;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskCategory;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.component.button.AjaxSubmitLinkButton;
import com.evolveum.midpoint.web.component.button.ButtonType;
import com.evolveum.midpoint.web.component.util.LoadableModel;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.resource.img.ImgResources;
import com.evolveum.midpoint.web.security.SecurityUtils;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.MisfireActionType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ScheduleType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.TaskBindingType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.TaskExecutionStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.TaskRecurrenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.TaskType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ThreadStopActionType;

/**
 * @author lazyman
 * @author mserbak
 */
public class PageTaskAdd extends PageAdminTasks {
	private static final long serialVersionUID = 2317887071933841581L;

	private static final Trace LOGGER = TraceManager.getTrace(PageTaskAdd.class);
	private static final String DOT_CLASS = PageTaskAdd.class.getName() + ".";
	private static final String OPERATION_LOAD_RESOURCES = DOT_CLASS + "createResourceList";
	private static final String OPERATION_SAVE_TASK = DOT_CLASS + "saveTask";
	private IModel<TaskAddDto> model;

	public PageTaskAdd() {
		model = new LoadableModel<TaskAddDto>(false) {

			@Override
			protected TaskAddDto load() {
				return loadTask();
			}
		};
		initLayout();
	}

	private TaskAddDto loadTask() {
		return new TaskAddDto();
	}

	private void initLayout() {
		Form mainForm = new Form("mainForm");
		add(mainForm);

		final DropDownChoice resource = new DropDownChoice("resource",
				new PropertyModel<TaskAddResourcesDto>(model, "resource"),
				new AbstractReadOnlyModel<List<TaskAddResourcesDto>>() {

					@Override
					public List<TaskAddResourcesDto> getObject() {
						return createResourceList();
					}
				}, new IChoiceRenderer<TaskAddResourcesDto>() {

					@Override
					public Object getDisplayValue(TaskAddResourcesDto dto) {
						return dto.getName();
					}

					@Override
					public String getIdValue(TaskAddResourcesDto dto, int index) {
						return Integer.toString(index);
					}
				});
		resource.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isEnabled() {
				TaskAddDto dto = model.getObject();
				boolean sync = TaskCategory.LIVE_SYNCHRONIZATION.equals(dto.getCategory());
				boolean recon = TaskCategory.RECONCILIATION.equals(dto.getCategory());
                boolean importAccounts = TaskCategory.IMPORTING_ACCOUNTS.equals(dto.getCategory());
				return sync || recon || importAccounts;
			}
		});
		resource.setOutputMarkupId(true);
		mainForm.add(resource);
		DropDownChoice type = new DropDownChoice("category", new PropertyModel<String>(model, "category"),
				new AbstractReadOnlyModel<List<String>>() {

					@Override
					public List<String> getObject() {
						return createCategoryList();
					}
				}, new IChoiceRenderer<String>() {

					@Override
					public Object getDisplayValue(String item) {
						return PageTaskAdd.this.getString("pageTask.category." + item);
					}

					@Override
					public String getIdValue(String item, int index) {
						return Integer.toString(index);
					}

				});
		type.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(resource);
			}
		});
        type.setRequired(true);
		mainForm.add(type);

		TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
		name.setRequired(true);
		mainForm.add(name);

		initScheduling(mainForm);
		initAdvanced(mainForm);

		initButtons(mainForm);
	}

	private void initScheduling(final Form mainForm) {
		final WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		mainForm.add(container);

		final IModel<Boolean> recurringCheck = new PropertyModel<Boolean>(model, "reccuring");
		final IModel<Boolean> boundCheck = new PropertyModel<Boolean>(model, "bound");

		final WebMarkupContainer boundContainer = new WebMarkupContainer("boundContainer");
		boundContainer.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return recurringCheck.getObject();
			}

		});
		boundContainer.setOutputMarkupId(true);
		container.add(boundContainer);

		final WebMarkupContainer intervalContainer = new WebMarkupContainer("intervalContainer");
		intervalContainer.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return recurringCheck.getObject();
			}

		});
		intervalContainer.setOutputMarkupId(true);
		container.add(intervalContainer);

		final WebMarkupContainer cronContainer = new WebMarkupContainer("cronContainer");
		cronContainer.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return recurringCheck.getObject() && !boundCheck.getObject();
			}

		});
		cronContainer.setOutputMarkupId(true);
		container.add(cronContainer);

		AjaxCheckBox recurring = new AjaxCheckBox("recurring", recurringCheck) {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(container);
			}
		};
		mainForm.add(recurring);

		AjaxCheckBox bound = new AjaxCheckBox("bound", boundCheck) {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(container);
			}
		};
		boundContainer.add(bound);
		
		final Image boundHelp = new Image("boundHelp", new PackageResourceReference(ImgResources.class,
				ImgResources.TOOLTIP_INFO));
		boundHelp.setOutputMarkupId(true);
		boundHelp.add(new AttributeAppender("original-title", getString("pageTask.boundHelp")));
		boundHelp.add(new AbstractDefaultAjaxBehavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				String js = "$('#"+ boundHelp.getMarkupId() +"').tipsy()";
				response.render(OnDomReadyHeaderItem.forScript(js));
				super.renderHead(component, response);
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
			}
		});
		boundContainer.add(boundHelp);

		TextField<Integer> interval = new TextField<Integer>("interval",
				new PropertyModel<Integer>(model, "interval"));
		interval.add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
		intervalContainer.add(interval);

		TextField<String> cron = new TextField<String>("cron", new PropertyModel<String>(
				model, "cron"));
		cron.add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
//		if (recurringCheck.getObject() && !boundCheck.getObject()) {
//			cron.setRequired(true);
//		}
		cronContainer.add(cron);
		
		final Image cronHelp = new Image("cronHelp", new PackageResourceReference(ImgResources.class,
				ImgResources.TOOLTIP_INFO));
		cronHelp.setOutputMarkupId(true);
		cronHelp.add(new AttributeAppender("original-title", getString("pageTask.cronHelp")));
		cronHelp.add(new AbstractDefaultAjaxBehavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				String js = "$('#"+ cronHelp.getMarkupId() +"').tipsy()";
				response.render(OnDomReadyHeaderItem.forScript(js));
				super.renderHead(component, response);
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
			}
		});
		cronContainer.add(cronHelp);

		final DateTimeField notStartBefore = new DateTimeField("notStartBeforeField",
				new PropertyModel<Date>(model, "notStartBefore")) {
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				return DateTextField.forDatePattern(id, dateFieldModel, "dd/MMM/yyyy"); // todo i18n
			}
		};
		notStartBefore.setOutputMarkupId(true);
		mainForm.add(notStartBefore);

		final DateTimeField notStartAfter = new DateTimeField("notStartAfterField", new PropertyModel<Date>(
				model, "notStartAfter")) {
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				return DateTextField.forDatePattern(id, dateFieldModel, "dd/MMM/yyyy"); // todo i18n
			}
		};
		notStartAfter.setOutputMarkupId(true);
		mainForm.add(notStartAfter);

        mainForm.add(new StartEndDateValidator(notStartBefore, notStartAfter));
        mainForm.add(new ScheduleValidator(getTaskManager(), recurring, bound, interval, cron));

    }

	private void initAdvanced(Form mainForm) {
		CheckBox runUntilNodeDown = new CheckBox("runUntilNodeDown", new PropertyModel<Boolean>(model,
				"runUntilNodeDown"));
		mainForm.add(runUntilNodeDown);

		final IModel<Boolean> createSuspendedCheck = new PropertyModel<Boolean>(model, "suspendedState");
		CheckBox createSuspended = new CheckBox("createSuspended", createSuspendedCheck);
		mainForm.add(createSuspended);

		DropDownChoice threadStop = new DropDownChoice("threadStop", new Model<ThreadStopActionType>() {

			@Override
			public ThreadStopActionType getObject() {
				TaskAddDto dto = model.getObject();
//				if (dto.getThreadStop() == null) {
//					if (!dto.getRunUntilNodeDown()) {
//						dto.setThreadStop(ThreadStopActionType.RESTART);
//					} else {
//						dto.setThreadStop(ThreadStopActionType.CLOSE);
//					}
//				}
				return dto.getThreadStop();
			}

			@Override
			public void setObject(ThreadStopActionType object) {
				model.getObject().setThreadStop(object);
			}
		}, WebMiscUtil.createReadonlyModelFromEnum(ThreadStopActionType.class),
				new EnumChoiceRenderer<ThreadStopActionType>(PageTaskAdd.this));
		mainForm.add(threadStop);

		mainForm.add(new TsaValidator(runUntilNodeDown, threadStop));

		DropDownChoice misfire = new DropDownChoice("misfireAction", new PropertyModel<MisfireActionType>(
				model, "misfireAction"), WebMiscUtil.createReadonlyModelFromEnum(MisfireActionType.class),
				new EnumChoiceRenderer<MisfireActionType>(PageTaskAdd.this));
		mainForm.add(misfire);
	}

	private void initButtons(final Form mainForm) {
		AjaxSubmitLinkButton saveButton = new AjaxSubmitLinkButton("saveButton", ButtonType.POSITIVE,
				createStringResource("pageTask.button.save")) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				savePerformed(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(getFeedbackPanel());
			}
		};
		mainForm.add(saveButton);

		AjaxLinkButton backButton = new AjaxLinkButton("backButton",
				createStringResource("pageTask.button.back")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(PageTasks.class);
			}
		};
		mainForm.add(backButton);
	}

	private List<String> createCategoryList() {
		List<String> categories = new ArrayList<String>();

		// todo change to something better and add i18n
//		TaskManager manager = getTaskManager();
//		List<String> list = manager.getAllTaskCategories();
//		if (list != null) {
//			Collections.sort(list);
//			for (String item : list) {
//				if (item != TaskCategory.IMPORT_FROM_FILE && item != TaskCategory.WORKFLOW) {
//					categories.add(item);
//				}
//			}
//		}
        categories.add(TaskCategory.LIVE_SYNCHRONIZATION);
        categories.add(TaskCategory.RECONCILIATION);
        categories.add(TaskCategory.IMPORTING_ACCOUNTS);
        categories.add(TaskCategory.USER_RECOMPUTATION);
        categories.add(TaskCategory.DEMO);
		return categories;
	}

	private List<TaskAddResourcesDto> createResourceList() {
		OperationResult result = new OperationResult(OPERATION_LOAD_RESOURCES);
		Task task = createSimpleTask(OPERATION_LOAD_RESOURCES);
		List<PrismObject<ResourceType>> resources = null;
		List<TaskAddResourcesDto> resourceList = new ArrayList<TaskAddResourcesDto>();

		try {
			resources = getModelService().searchObjects(ResourceType.class, new ObjectQuery(), null, task, result);
			result.recomputeStatus();
		} catch (Exception ex) {
			result.recordFatalError("Couldn't get resource list.", ex);
			LoggingUtils.logException(LOGGER, "Couldn't get resource list", ex);
		}

		// todo show result somehow...
		// if (!result.isSuccess()) {
		// showResult(result);
		// }
		if (resources != null) {
			ResourceType item = null;
			for (PrismObject<ResourceType> resource : resources) {
				item = resource.asObjectable();
				resourceList.add(new TaskAddResourcesDto(item.getOid(), WebMiscUtil.getOrigStringFromPoly(item.getName())));
			}
		}
		return resourceList;
	}

	private void savePerformed(AjaxRequestTarget target) {
		LOGGER.debug("Saving new task.");
		OperationResult result = new OperationResult(OPERATION_SAVE_TASK);
		TaskAddDto dto = model.getObject();
		TaskType task = createTask(dto);
        Task operationTask = createSimpleTask(OPERATION_SAVE_TASK);

		try {
			getPrismContext().adopt(task);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Adding new task.");
			}
            getModelService().executeChanges(prepareChangesToExecute(task), null, operationTask, result);
			result.recomputeStatus();
			setResponsePage(PageTasks.class);
		} catch (Exception ex) {
			result.recomputeStatus();
			result.recordFatalError("Unable to save task.", ex);
			LoggingUtils.logException(LOGGER, "Couldn't add new task", ex);
		}
		showResultInSession(result);
		target.add(getFeedbackPanel());
	}

    private Collection<ObjectDelta<? extends ObjectType>> prepareChangesToExecute(TaskType taskToBeAdded) {
        return Arrays.asList((ObjectDelta<? extends ObjectType>) ObjectDelta.createAddDelta(taskToBeAdded.asPrismObject()));
    }

    private TaskType createTask(TaskAddDto dto) {
		TaskType task = new TaskType();
		MidPointPrincipal owner = SecurityUtils.getPrincipalUser();

		ObjectReferenceType ownerRef = new ObjectReferenceType();
		ownerRef.setOid(owner.getOid());
		ownerRef.setType(owner.getUser().COMPLEX_TYPE);
		task.setOwnerRef(ownerRef);

    	task.setCategory(dto.getCategory());
        String handlerUri = getTaskManager().getHandlerUriForCategory(dto.getCategory());
        if (handlerUri == null) {
            throw new SystemException("Cannot determine task handler URI for category " + dto.getCategory());
        }
        task.setHandlerUri(handlerUri);

		ObjectReferenceType objectRef = null;
		if(dto.getResource() != null){
			objectRef = new ObjectReferenceType();
			objectRef.setOid(dto.getResource().getOid());
			task.setObjectRef(objectRef);
            // todo set also object ref type
		}

		task.setName(WebMiscUtil.createPolyFromOrigString(dto.getName()));

		task.setRecurrence(dto.getReccuring() ? TaskRecurrenceType.RECURRING : TaskRecurrenceType.SINGLE);
		task.setBinding(dto.getBound() ? TaskBindingType.TIGHT : TaskBindingType.LOOSE);

		ScheduleType schedule = new ScheduleType();
		schedule.setInterval(dto.getInterval());
		schedule.setCronLikePattern(dto.getCron());
		schedule.setEarliestStartTime(MiscUtil.asXMLGregorianCalendar(dto.getNotStartBefore()));
		schedule.setLatestStartTime(MiscUtil.asXMLGregorianCalendar(dto.getNotStartAfter()));
		schedule.setMisfireAction(dto.getMisfireAction());
		task.setSchedule(schedule);

		if(dto.getSuspendedState()){
			task.setExecutionStatus(TaskExecutionStatusType.SUSPENDED);
		} else {
			task.setExecutionStatus(TaskExecutionStatusType.RUNNABLE);
		}

        if (dto.getThreadStop() != null) {
		    task.setThreadStopAction(dto.getThreadStop());
        } else {
            // fill-in default
            if (dto.getRunUntilNodeDown() == true) {
                task.setThreadStopAction(ThreadStopActionType.CLOSE);
            } else {
                task.setThreadStopAction(ThreadStopActionType.RESTART);
            }
        }

		return task;
	}

	private static class EmptyOnBlurAjaxFormUpdatingBehaviour extends AjaxFormComponentUpdatingBehavior {

		public EmptyOnBlurAjaxFormUpdatingBehaviour() {
			super("onBlur");
		}

		@Override
		protected void onUpdate(AjaxRequestTarget target) {
		}
	}
}
