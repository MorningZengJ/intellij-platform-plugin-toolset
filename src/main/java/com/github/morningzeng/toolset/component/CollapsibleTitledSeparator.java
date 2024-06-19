package com.github.morningzeng.toolset.component;

import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts.Separator;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.dsl.builder.impl.CollapsibleTitledSeparatorImpl;
import com.intellij.util.Consumer;
import com.intellij.util.ui.IndentedIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Morning Zeng
 * @see CollapsibleTitledSeparatorImpl
 * @since 2024-06-17
 */
@SuppressWarnings("UnstableApiUsage")
public final class CollapsibleTitledSeparator extends TitledSeparator {

    private boolean expanded = true;

    public CollapsibleTitledSeparator(final String text) {
        this(text, null);
    }

    public CollapsibleTitledSeparator(@Separator final String text, final @Nullable JComponent labelFor) {
        super(text, labelFor);
        this.updateIcon();
        super.setLabelFocusable(true);

        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                expanded = !expanded;
                updateIcon();
            }
        });
    }


    public void addExpandedListener(final Consumer<Boolean> consumer) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                consumer.consume(expanded);
            }
        });
    }

    void updateIcon() {
        final Icon treeExpandedIcon = UIUtil.getTreeExpandedIcon();
        final Icon treeCollapsedIcon = UIUtil.getTreeCollapsedIcon();
        final int width = Math.max(treeExpandedIcon.getIconWidth(), treeCollapsedIcon.getIconWidth());
        Icon icon = expanded ? treeExpandedIcon : treeCollapsedIcon;
        final int space = width - icon.getIconWidth();
        if (space > 0) {
            final int left = space / 2;
            icon = new IndentedIcon(icon, JBUI.insets(0, left, 0, space - left));
        }
        super.getLabel().setIcon(icon);
        super.getLabel().setDisabledIcon(IconLoader.getTransparentIcon(icon, .5f));
    }

}
