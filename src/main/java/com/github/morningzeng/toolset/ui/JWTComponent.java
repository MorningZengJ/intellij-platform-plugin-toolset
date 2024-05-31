package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.HorizontalDoubleButton;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelComponent;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.config.JWTProp;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.dialog.JWTPropDialog;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
public class JWTComponent extends JBPanel<JBPanelWithEmptyText> {

    final LocalConfigFactory STATE_FACTORY = LocalConfigFactory.getInstance();
    private final Project project;

    private final ComboBox<JWTProp> signKeyComboBox = new ComboBox<>(STATE_FACTORY.jwtPropsMap().values().stream()
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(JWTProp::getSorted))
            .toArray(JWTProp[]::new));
    private final JButton jwtPropManageBtn = new JButton(General.Ellipsis);
    private final LabelTextArea jwtTextArea = new LabelTextArea("JWT");
    private final HorizontalDoubleButton btnBar = new HorizontalDoubleButton(new JButton("Resolve", IconC.DOUBLE_ARROW_DOWN), new JButton("Generate", IconC.DOUBLE_ARROW_UP));
    private final LabelTextArea headerTextArea = new LabelTextArea("Header");
    private final LabelTextArea payloadTextArea = new LabelTextArea("Payload");

    public JWTComponent(final Project project) {
        this.project = project;
        this.signKeyComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (Objects.isNull(value)) {
                return new JBLabel();
            }
            final String keyString = switch (value.signAlgorithm()) {
                case NONE -> "";
                case HS256, HS384, HS512 ->
                        "%s [%s]".formatted(StringUtils.maskSensitive(value.getSymmetricKey()), value.symmetricKeyType());
                default ->
                        "%s [ Private Key ] / %s [ Public Key ]".formatted(StringUtils.maskSensitive(value.getPrivateKey()), StringUtils.maskSensitive(value.getPublicKey()));
            };

            return new JBLabel("%s - %s ( %s )".formatted(value.getTitle(), value.getDesc(), keyString));
        });

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.signKeyComboBox)
                .newCell().weightX(0).add(this.jwtPropManageBtn)
                .newRow().fill(GridBag.BOTH).weightX(1).weightY(1).add(this.jwtTextArea)
                .newRow().weightY(0).add(new LabelComponent<>("", this.btnBar))
                .newRow().weightY(1).add(this.headerTextArea)
                .newRow().add(this.payloadTextArea);
        this.initEvent();
    }

    void initEvent() {
        this.jwtPropManageBtn.addActionListener(e -> {
            final JWTPropDialog dialog = new JWTPropDialog(this.project);
            dialog.showAndGet();
            this.refresh();
        });
        this.btnBar.first().addActionListener(e -> {
            // resolve
            final JWTProp item = this.signKeyComboBox.getItem();
            final JwtParser build = Jwts.parser()
                    .decryptWith(item.secretKeySpec())
                    .build();
            final Jwt<Header, Claims> headerClaims = build.parseUnsecuredClaims(this.jwtTextArea.getText());
            this.headerTextArea.setText(headerClaims.getHeader().toString());
            this.payloadTextArea.setText(headerClaims.getPayload().toString());
        });
        this.btnBar.second().addActionListener(e -> {
            // generate
        });
    }

    void refresh() {
        this.signKeyComboBox.removeAllItems();
        STATE_FACTORY.jwtPropsMap().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(JWTProp::getSorted))
                .forEach(this.signKeyComboBox::addItem);
    }
}
