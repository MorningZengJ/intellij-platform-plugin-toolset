package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxButton;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static com.github.morningzeng.toolset.utils.JacksonUtils.IGNORE_TRANSIENT_AND_NULL;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
public class JWTComponent extends JBPanel<JBPanelWithEmptyText> {

    final LocalConfigFactory.State state = LocalConfigFactory.getInstance().getState();
    private final Project project;

    private final LabelComponent<ComboBoxButton<JWTProp>> comboBoxButton = new LabelComponent<>(
            "Choose key", new ComboBoxButton<>(
            new JButton(General.Ellipsis), 0, state.jwtPropsMap().values().stream()
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(JWTProp::getSorted))
            .toArray(JWTProp[]::new)
    ));
    private final LabelTextArea jwtTextArea = new LabelTextArea("JWT");
    private final HorizontalDoubleButton btnBar = new HorizontalDoubleButton(new JButton("Resolve", IconC.DOUBLE_ANGLES_DOWN), new JButton("Generate", IconC.DOUBLE_ANGLES_UP));
    private final LabelTextArea headerTextArea = new LabelTextArea("Header");
    private final LabelTextArea payloadTextArea = new LabelTextArea("Payload");

    public JWTComponent(final Project project) {
        this.project = project;
        this.comboBoxButton.second().first().setRenderer((list, value, index, isSelected, cellHasFocus) -> {
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
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.comboBoxButton)
                .newRow().fill(GridBag.BOTH).weightX(1).weightY(1).add(this.jwtTextArea)
                .newRow().weightY(0).add(new LabelComponent<>("", this.btnBar))
                .newRow().weightY(1).add(this.headerTextArea)
                .newRow().add(this.payloadTextArea);
        this.initEvent();
    }

    void initEvent() {
        this.comboBoxButton.second().second().addActionListener(e -> {
            final JWTPropDialog dialog = new JWTPropDialog(this.project);
            dialog.showAndGet();
            this.refresh();
        });
        this.btnBar.first().addActionListener(e -> {
            // resolve
            final JWTProp item = this.comboBoxButton.second().first().getItem();
            if (Objects.isNull(item)) {
                return;
            }
            final JwtParser build = Jwts.parser()
                    .verifyWith(item.secretKeySpec())
                    .build();
            final Jwt<?, ?> parse = build.parse(this.jwtTextArea.getText());
            final Jwe<Claims> claimsJws = parse.accept(Jwe.CLAIMS);
//            final Jws<Claims> claimsJws = build.parseSignedClaims(this.jwtTextArea.getText());
            this.headerTextArea.setText(IGNORE_TRANSIENT_AND_NULL.toPrettyJson(claimsJws.getHeader()));
            this.payloadTextArea.setText(IGNORE_TRANSIENT_AND_NULL.toPrettyJson(claimsJws.getPayload()));
        });
        this.btnBar.second().addActionListener(e -> {
            // generate
        });
    }

    void refresh() {
        this.comboBoxButton.second().first().removeAllItems();
        state.jwtPropsMap().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(JWTProp::getSorted))
                .forEach(this.comboBoxButton.second().first()::addItem);
    }
}
