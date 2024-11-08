package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxButton;
import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxEditorTextField;
import com.github.morningzeng.toolset.component.AbstractComponent.EditorTextFieldButton;
import com.github.morningzeng.toolset.component.AbstractComponent.HorizontalDoubleButton;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelComponent;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComboBoxWithWidePopup;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI.Borders;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-30
 */
public sealed abstract class AbstractComponent<F extends JComponent, S extends JComponent> extends JBPanel<JBPanelWithEmptyText>
        permits ComboBoxButton, ComboBoxEditorTextField, EditorTextFieldButton, HorizontalDoubleButton, LabelComponent {
    protected final static int GAP = 5;
    protected final F f;
    protected final S s;

    public AbstractComponent(final F f, final S s) {
        this(f, s, GAP);
    }

    public AbstractComponent(final F f, final S s, final int gap) {
        super(new GridBagLayout());
        this.setBorder(Borders.empty(gap));

        this.f = f;
        this.s = s;
    }

    public F first() {
        return this.f;
    }

    public S second() {
        return this.s;
    }

    public non-sealed static class LabelComponent<T extends JComponent> extends AbstractComponent<JBLabel, T> {

        public LabelComponent(final String label, final T t) {
            this(label, t, GAP);
        }

        public LabelComponent(final String label, final T t, final int gap) {
            super(new JBLabel(label), t);
            this.setBorder(Borders.empty(1, gap));

            final Dimension labelDimension = new Dimension(125, this.f.getHeight());
            this.f.setPreferredSize(labelDimension);

            GridBagUtils.builder(this)
                    .newRow(row -> row.fill(GridBagFill.BOTH)
                            .newCell().weightX(0).weightY(1).add(this.f)
                            .newCell().weightX(1).weightY(1).add(
                                    Optional.of(this.s)
                                            .filter(ScrollSupport.class::isInstance)
                                            .map(ScrollSupport.class::cast)
                                            .<JComponent>map(ScrollSupport::verticalAsNeededScrollPane)
                                            .orElse(this.s)
                            ));
        }

        public void setLabel(final String label) {
            this.f.setText(label);
            this.f.setToolTipText(label);
        }

        public void labelConsumer(final Consumer<JBLabel> consumer) {
            consumer.accept(this.f);
        }

        public <R> R labelFunction(final Function<JBLabel, R> function) {
            return function.apply(this.f);
        }

        public void tConsumer(final Consumer<T> consumer) {
            consumer.accept(this.s);
        }

        public <R> R tFunction(final Function<T, R> function) {
            return function.apply(this.s);
        }

    }

    public final static class ComboBoxButton<T> extends AbstractComponent<ComboBox<T>, JButton> {

        @SafeVarargs
        public ComboBoxButton(final JButton button, final T... ts) {
            this(button, GAP, ts);
        }

        @SafeVarargs
        public ComboBoxButton(final JButton button, final int gap, final T... ts) {
            super(new ComboBox<>(ts), button, gap);
            GridBagUtils.builder(this)
                    .newRow(row -> row.fill(GridBagFill.BOTH)
                            .newCell().weightX(1).weightY(1).add(super.f)
                            .newCell().weightX(0).weightY(1).add(super.s));
        }

        public ComboBoxButton(final JButton button, final Stream<T> stream, final IntFunction<T[]> generator) {
            this(button, stream.toArray(generator));
        }

        public ComboBoxButton(final JButton button, final Collection<T> ts, final IntFunction<T[]> generator) {
            this(button, ts.toArray(generator));
        }

    }

    public final static class ComboBoxEditorTextField<T> extends AbstractComponent<ComboBox<T>, EditorTextField> {

        @SafeVarargs
        public ComboBoxEditorTextField(final String placeholder, final JButton button, final T... ts) {
            this(placeholder, button, GAP, ts);
        }

        @SafeVarargs
        public ComboBoxEditorTextField(final String placeholder, final JButton button, final int gap, final T... ts) {
            super(new ComboBox<>(ts), textField(placeholder), gap);
            GridBagUtils.builder(this)
                    .newRow(row -> row.fill(GridBagFill.BOTH)
                            .newCell().add(super.f)
                            .newCell().weightX(1).add(super.s)
                            .newCell().weightX(0).add(button));
        }

        private static EditorTextField textField(String placeholder) {
            final EditorTextField textField = new EditorTextField();
            textField.setPlaceholder(placeholder);
            return textField;
        }

        public String getText() {
            return this.s.getText();
        }

        public void setText(final String text) {
            this.s.setText(text);
        }

        public T getItem() {
            return this.f.getItem();
        }

        public void setItem(final T t) {
            this.f.setItem(t);
        }
    }

    public final static class HorizontalDoubleButton extends AbstractComponent<JButton, JButton> {

        public HorizontalDoubleButton(final JButton f, final JButton s) {
            this(f, s, GAP);
        }

        public HorizontalDoubleButton(final JButton f, final JButton s, final int gap) {
            super(f, s, gap);
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            this.add(f);
            this.add(s);
        }
    }

    public static sealed abstract class AbstractLabelTextComponent<T extends JTextComponent> extends LabelComponent<T>
            permits LabelTextField {

        public AbstractLabelTextComponent(final String label, final T t) {
            super(label, t);
        }

        public String getText() {
            return super.tFunction(JTextComponent::getText);
        }

        public void setText(final String text) {
            super.tConsumer(t -> {
                t.setText(text);
                t.setToolTipText(super.labelFunction(JBLabel::getText));
            });
        }

    }

    public final static class LabelTextField extends AbstractLabelTextComponent<JBTextField> {

        public LabelTextField(final String label) {
            super(label, new JBTextField());
        }

        public LabelTextField(final String label, final String text) {
            super(label, new JBTextField(text));
        }

    }

    public final static class LabelTextArea extends LabelComponent<LanguageTextArea> {

        public LabelTextArea(final Project project, final String label) {
            this(project, label, "");
        }

        public LabelTextArea(final Project project, final String label, final String text) {
            super(label, new LanguageTextArea(project, text));
            this.f.setPreferredSize(new Dimension(125, this.f.getHeight()));
        }

        public String getText() {
            return super.tFunction(LanguageTextArea::getText);
        }

        public void setText(final String text) {
            super.tConsumer(t -> {
                t.setText(text);
                t.setToolTipText(super.labelFunction(JBLabel::getText));
            });
        }

    }

    public final static class LabelComboBox<T> extends LabelComponent<ComboBox<T>> {

        @SafeVarargs
        public LabelComboBox(final String label, final T... ts) {
            super(label, new ComboBox<>(ts));
        }

        public T getItem() {
            return super.tFunction(ComboBoxWithWidePopup::getItem);
        }

        public void setSelectedItem(final T t) {
            super.tConsumer(comboBox -> comboBox.setSelectedItem(t));
        }
    }

    public final static class EditorTextFieldButton extends AbstractComponent<EditorTextField, JButton> {

        public EditorTextFieldButton(final String placeholder, final JButton b) {
            this(placeholder, b, GAP);
        }

        public EditorTextFieldButton(final String placeholder, final JButton b, final int gap) {
            super(textField(placeholder), b, gap);
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            this.add(f);
            this.add(s);
        }

        private static EditorTextField textField(String placeholder) {
            final EditorTextField textField = new EditorTextField();
            textField.setPlaceholder(placeholder);
            return textField;
        }

    }

}
