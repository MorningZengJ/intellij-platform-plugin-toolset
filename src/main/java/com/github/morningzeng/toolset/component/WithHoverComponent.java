package com.github.morningzeng.toolset.component;

import com.intellij.ui.components.JBLayeredPane;

import javax.swing.JComponent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Morning Zeng
 * @since 2024-05-24
 */
public final class WithHoverComponent extends JBLayeredPane {

    private final JComponent component;
    private final JComponent hoverComponent;
    private final int width;
    private final int height;

    public WithHoverComponent(final JComponent component, final JComponent hoverComponent, final int width, final int height) {
        this.component = component;
        this.hoverComponent = hoverComponent;
        this.width = width;
        this.height = height;

        this.add(this.component, DEFAULT_LAYER);
        this.add(this.hoverComponent, POPUP_LAYER);
        this.componentFocusEvent();
        this.resized();
    }

    void componentFocusEvent() {
        this.component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                hoverComponent.setVisible(false);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                hoverComponent.setVisible(true);
            }
        });
    }

    void resized() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                // 获取 JBLayeredPane 新的大小
                int width = WithHoverComponent.this.getWidth();
                int height = WithHoverComponent.this.getHeight();

                // 更新 decryptArea 和 contextTypeComboBox 的大小和位置
                component.setBounds(0, 0, width, height);
                hoverComponent.setBounds(width - (int) (WithHoverComponent.this.width * 1.1), (int) (WithHoverComponent.this.width * .1), WithHoverComponent.this.width, WithHoverComponent.this.height);

                // 调用 repaint 以便应用新的大小和位置
                WithHoverComponent.this.repaint();
            }
        });
    }
}
