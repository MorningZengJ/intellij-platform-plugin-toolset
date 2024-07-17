package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.DateFormatter;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.CheckBoxBar;
import com.github.morningzeng.toolset.component.DatePicker;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.component.RadioBar;
import com.github.morningzeng.toolset.model.Remind;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.CheckBox;
import com.intellij.util.ui.GridBag;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-07-12
 */
public final class RemindsComponent extends AbstractTreePanelComponent<Remind> {

    private final List<ScheduledFuture<?>> childrenTasks = Lists.newArrayList();
    private ScheduledFuture<?> scheduledFuture;

    public RemindsComponent(Project project) {
        super(project, Remind.class, "remind-tree-splitter");
        this.schedule();
    }

    @SneakyThrows
    public void schedule() {
        if (Objects.nonNull(this.scheduledFuture)) {
            this.scheduledFuture.cancel(false);
            while (!this.scheduledFuture.isCancelled()) {
                TimeUnit.MILLISECONDS.sleep(500);
            }
        }
        this.childrenTasks.removeIf(sf -> {
            sf.cancel(false);
            return true;
        });
        this.scheduledFuture = EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(() -> {
            final LocalDateTime now = LocalDateTime.now();
            this.components.keySet().stream()
                    .filter(Predicate.not(Remind::isGroup))
                    .filter(remind -> Objects.nonNull(remind.getDate()))
                    .filter(remind -> remind.getDate().isAfter(now) || remind.isCycle())
                    .forEach(remind -> {
                        final LocalDateTime date = remind.getDate();
                        if (date.isAfter(now)) {
                            final long delay = now.until(date, ChronoUnit.SECONDS);
                            if (delay <= TimeUnit.MINUTES.toSeconds(15)) {
                                this.notification(remind, delay);
                            }
                            return;
                        }
                        if (Objects.isNull(remind.getCycleUnit())) {
                            return;
                        }
                        final long until = date.until(now, remind.getCycleUnit());
                        LocalDateTime plusDate = date.plus(until, remind.getCycleUnit());
                        if (plusDate.isBefore(now)) {
                            plusDate = plusDate.plus(1, remind.getCycleUnit());
                        }
                        final long delay = now.until(plusDate, ChronoUnit.SECONDS);
                        if (delay <= TimeUnit.MINUTES.toSeconds(15)) {
                            if (ChronoUnit.DAYS.equals(remind.getCycleUnit())) {
                                if (remind.getCycleDayOfWeeks().contains(plusDate.getDayOfWeek())) {
                                    this.notification(remind, delay);
                                }
                                return;
                            }
                            this.notification(remind, delay);
                        }
                    });
        }, 0, 15, TimeUnit.MINUTES);
    }

    private void notification(final Remind remind, final long delay) {
        final ScheduledFuture<?> scheduled = EdtExecutorService.getScheduledExecutorInstance().schedule(() -> {
            final Notification notification = new Notification("remind-notify", "You have a reminder", remind.getContent(), NotificationType.INFORMATION)
                    .addAction(new NotificationAction("Alert me in 5 minutes") {
                        @Override
                        public void actionPerformed(@NotNull final AnActionEvent e, @NotNull final Notification notification) {
                            notification(remind, TimeUnit.MINUTES.toSeconds(5));
                            notification.expire();
                        }
                    });
            notification.notify(project);
        }, delay, TimeUnit.SECONDS);
        this.childrenTasks.add(scheduled);
    }

    @Override
    AnAction[] actions() {
        final AnAction[] actions = super.actions();
        return Stream.concat(Stream.of(this.addAction()), Stream.of(actions))
                .toArray(AnAction[]::new);
    }

    @Override
    void reloadFileEvent(final AnActionEvent e) {
        super.reloadFileEvent(e);
        this.schedule();
    }

    @Override
    JBPanel<?> childPanel(final Remind remind) {
        return new RemindPanel(this.project, remind, this::schedule);
    }

    @Override
    String configFileDirectory() {
        return "Reminds";
    }

    AnAction addAction() {
        return ActionUtils.drawerActions(
                "Add", "Add Group And Item", IconC.ADD_GREEN,
                new SingleTextFieldDialogAction(this.project, "Add Group", "Group", group -> {
                    final Remind remind = Remind.builder()
                            .name(group)
                            .directory(true)
                            .build();
                    getOrCreatePanel(remind, true);
                }),
                new AnAction("Add Item") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        final Remind selectedValue = tree.getSelectedValue();

                        final String name = Optional.ofNullable(selectedValue)
                                .map(sv -> sv.isGroup() ? sv : sv.getParent())
                                .map(sv -> "%s#%s".formatted(sv.name(), Optional.ofNullable(sv.getChildren()).map(List::size).orElse(0) + 1))
                                .orElse("request#%s".formatted(tree.childrenCount() + 1));

                        final Remind httpBean = Remind.builder()
                                .name(name)
                                .build();
                        getOrCreatePanel(httpBean, true);
                    }
                }
        );
    }

    static class RemindPanel extends JBPanel<JBPanelWithEmptyText> {
        private final Remind remind;
        private final Runnable rerun;

        private final EditorTextField nameTextField = new EditorTextField();
        private final TextFieldWithBrowseButton datepickerWithClock = new TextFieldWithBrowseButton(new ExtendableTextField(20)) {
            @Override
            protected @NotNull Icon getDefaultIcon() {
                return IconC.CLOCK_COLOR;
            }

            @Override
            protected @NotNull Icon getHoveredIcon() {
                return IconC.CLOCK_COLOR;
            }
        };
        private final CheckBox cycleCheckBox;
        private final RadioBar<ChronoUnit> cycleUnitRadio;
        private final CheckBoxBar<DayOfWeek> dayOfWeekCheckBoxBar;
        private final LanguageTextArea contentTextArea;

        RemindPanel(final Project project, final Remind remind, final Runnable rerun) {
            super(new GridBagLayout());
            this.remind = remind;
            this.rerun = rerun;
            this.cycleCheckBox = new CheckBox("Cycle", this.remind, "cycle");
            this.cycleUnitRadio = new RadioBar<>(this.remind.getCycleUnit(), ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS);
            this.dayOfWeekCheckBoxBar = new CheckBoxBar<>(
                    Arrays.stream(DayOfWeek.values()).collect(
                            Collectors.toMap(Function.identity(), this.remind.getCycleDayOfWeeks()::contains)
                    ));
            this.contentTextArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, this.remind.getContent());
            this.contentTextArea.setPlaceholder("Please enter the details of the agent/reminder...");
            this.nameTextField.setPlaceholder("Please enter a name for the to-do/reminder...");
            this.nameTextField.setText(this.remind.getName());
            this.datepickerWithClock.setText(Optional.ofNullable(this.remind.getDate())
                    .map(DateFormatter.YYYY_MM_DD_HH_MM_SS::format)
                    .orElse(""));

            this.cycleUnitRadio.setEnabled(this.remind.isCycle());
            this.dayOfWeekCheckBoxBar.setEnabled(this.remind.isCycle());
            this.dayOfWeekCheckBoxBar.setVisible(ChronoUnit.DAYS.equals(this.remind.getCycleUnit()));

            this.initEvent();

            GridLayoutUtils.builder()
                    .container(this).fill(GridBag.HORIZONTAL).weightX(1).gridWidth(2).add(this.nameTextField)
                    .newRow().weightX(0).add(this.datepickerWithClock)
                    .newCell().add(new JBPanel<>() {{
                        this.setLayout(new BorderLayout());
                        this.add(new JBPanel<>() {{
                            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
                            this.add(cycleCheckBox);
                            this.add(cycleUnitRadio);
                        }}, BorderLayout.BEFORE_LINE_BEGINS);
                        this.add(dayOfWeekCheckBoxBar, BorderLayout.AFTER_LAST_LINE);
                    }})
                    .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(2).add(this.contentTextArea);
        }

        private void initEvent() {
            this.nameTextField.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull final DocumentEvent event) {
                    remind.setName(nameTextField.getText());
                }
            });
            this.datepickerWithClock.getChildComponent().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    final String text = datepickerWithClock.getText();
                    if (StringUtil.isEmpty(text)) {
                        return;
                    }
                    try {
                        final LocalDateTime date = LocalDateTime.parse(text, DateFormatter.YYYY_MM_DD_HH_MM_SS);
                        remind.setDate(date);
                        rerun.run();
                    } catch (final Exception ex) {
                        Messages.showErrorDialog(ex.getMessage(), "Datetime Format Error");
                        final String dateText = Optional.ofNullable(remind.getDate())
                                .map(DateFormatter.YYYY_MM_DD_HH_MM_SS::format)
                                .orElse("");
                        datepickerWithClock.setText(dateText);
                    }
                }
            });
            this.datepickerWithClock.addActionListener(
                    e -> this.createDatePickerPopup(new DatePicker(this.remind.getDate()), this.datepickerWithClock, localDateTime -> {
                        this.remind.setDate(localDateTime);
                        this.datepickerWithClock.setText(DateFormatter.YYYY_MM_DD_HH_MM_SS.format(localDateTime));
                        rerun.run();
                    })
            );
            this.contentTextArea.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull final DocumentEvent event) {
                    remind.setContent(contentTextArea.getText());
                }
            });
            this.cycleCheckBox.addChangeListener(e -> {
                this.rerun.run();
                this.cycleUnitRadio.setEnabled(this.remind.isCycle());
            });
            this.cycleUnitRadio.addItemListener(chronoUnit -> {
                this.rerun.run();
                this.remind.setCycleUnit(chronoUnit);
            });
            this.cycleUnitRadio.addItemListener(ChronoUnit.DAYS, chronoUnit -> {
                this.dayOfWeekCheckBoxBar.setEnabled(this.remind.isCycle());
                this.dayOfWeekCheckBoxBar.setVisible(ChronoUnit.DAYS.equals(chronoUnit));
            });
            this.dayOfWeekCheckBoxBar.addItemListener((dayOfWeek, selected) -> {
                if (selected) {
                    this.remind.getCycleDayOfWeeks().add(dayOfWeek);
                } else {
                    this.remind.getCycleDayOfWeeks().remove(dayOfWeek);
                }
            });
        }

        private void createDatePickerPopup(final DatePicker datePicker, final JComponent component, final Consumer<LocalDateTime> closedCallback) {
            final JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(datePicker, null)
                    .setResizable(true)
                    .setMovable(true)
                    .setRequestFocus(true)
                    .createPopup();
            popup.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull final LightweightWindowEvent event) {
                    final LocalDateTime dateTime = datePicker.getDateTime();
                    closedCallback.accept(dateTime);
                }
            });
            popup.show(new RelativePoint(component, new Point(component.getWidth() / 2, component.getHeight())));
        }

    }

}
