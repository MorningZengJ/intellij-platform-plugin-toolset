package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxButton;
import com.github.morningzeng.toolset.component.AbstractComponent.HorizontalDoubleButton;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelComponent;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComboBoxWithWidePopup;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
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
        permits ComboBoxButton, HorizontalDoubleButton, LabelComponent {
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
            this.setBorder(Borders.empty(1, GAP));

            final Dimension labelDimension = new Dimension(125, this.f.getHeight());
            this.f.setPreferredSize(labelDimension);

            GridLayoutUtils.builder()
                    .container(this).fill(GridBag.BOTH).weightX(0).weightY(1).add(this.f)
                    .newCell().weightX(1).weightY(1).add(
                            Optional.of(this.s)
                                    .filter(ScrollSupport.class::isInstance)
                                    .map(ScrollSupport.class::cast)
                                    .<JComponent>map(ScrollSupport::scrollPane)
                                    .orElse(this.s)
                    );
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
            GridLayoutUtils.builder()
                    .container(this).fill(GridBag.BOTH).weightX(1).weightY(1).add(super.f)
                    .newCell().weightX(0).weightY(1).add(super.s);
        }

        public ComboBoxButton(final JButton button, final Stream<T> stream, final IntFunction<T[]> generator) {
            this(button, stream.toArray(generator));
        }

        public ComboBoxButton(final JButton button, final Collection<T> ts, final IntFunction<T[]> generator) {
            this(button, ts.toArray(generator));
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
            permits LabelTextArea, LabelTextField {

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

    public final static class LabelTextArea extends AbstractLabelTextComponent<FocusColorTextArea> {

        public LabelTextArea(final String label) {
            super(label, FocusColorTextArea.builder().row(5).column(50).focusListener());
        }

        public LabelTextArea(final String label, final String text) {
            super(label, FocusColorTextArea.builder().row(5).column(50).text(text).focusListener());
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

}
