package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.component.AbstractLabelComponent.AbstractLabelTextComponent;
import com.github.morningzeng.toolset.component.AbstractLabelComponent.LabelComboBox;
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

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Morning Zeng
 * @since 2024-05-30
 */
public sealed abstract class AbstractLabelComponent<T extends JComponent> extends JBPanel<JBPanelWithEmptyText> permits AbstractLabelTextComponent, LabelComboBox {

    private final static int GAP = 5;
    private final JBLabel label;
    private final T t;

    public AbstractLabelComponent(final JBLabel label, final T t) {
        super(new GridBagLayout());
        this.setBorder(Borders.empty(1, GAP));

        this.label = label;
        this.t = t;

        final Dimension labelDimension = new Dimension(125, this.label.getHeight());
        this.label.setPreferredSize(labelDimension);

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(0).weightY(1).add(this.label)
                .newCell().weightX(1).weightY(1).add(
                        Optional.of(this.t)
                                .filter(ScrollSupport.class::isInstance)
                                .map(ScrollSupport.class::cast)
                                .<JComponent>map(ScrollSupport::scrollPane)
                                .orElse(this.t)
                );
    }

    public void setLabel(final String label) {
        this.label.setText(label);
        this.label.setToolTipText(label);
    }

    public void labelConsumer(final Consumer<JBLabel> consumer) {
        consumer.accept(this.label);
    }

    public <R> R labelFunction(final Function<JBLabel, R> function) {
        return function.apply(this.label);
    }

    public void tConsumer(final Consumer<T> consumer) {
        consumer.accept(this.t);
    }

    public <R> R tFunction(final Function<T, R> function) {
        return function.apply(this.t);
    }

    public static sealed abstract class AbstractLabelTextComponent<T extends JTextComponent> extends AbstractLabelComponent<T> permits LabelTextArea, LabelTextField {

        public AbstractLabelTextComponent(final JBLabel label, final T t) {
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
            super(new JBLabel(label), new JBTextField());
        }

        public LabelTextField(final String label, final String text) {
            super(new JBLabel(label), new JBTextField(text));
        }

    }

    public final static class LabelTextArea extends AbstractLabelTextComponent<FocusColorTextArea> {

        public LabelTextArea(final String label) {
            super(new JBLabel(label), FocusColorTextArea.builder().row(5).column(50).focusListener());
        }

        public LabelTextArea(final String label, final String text) {
            super(new JBLabel(label), FocusColorTextArea.builder().row(5).column(50).text(text).focusListener());
        }

    }

    public final static class LabelComboBox<T> extends AbstractLabelComponent<ComboBox<T>> {

        @SafeVarargs
        public LabelComboBox(final String label, final T... ts) {
            super(new JBLabel(label), new ComboBox<>(ts));
        }

        public T getItem() {
            return super.tFunction(ComboBoxWithWidePopup::getItem);
        }

        public void setSelectedItem(final T t) {
            super.tConsumer(comboBox -> comboBox.setSelectedItem(t));
        }
    }

}
