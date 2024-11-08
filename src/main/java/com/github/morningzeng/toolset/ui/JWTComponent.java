package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.dialog.JWTPropDialog;
import com.github.morningzeng.toolset.model.JWTProp;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTabbedPane;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.morningzeng.toolset.utils.JacksonUtils.IGNORE_TRANSIENT_AND_NULL;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
public final class JWTComponent extends AbstractCryptoPropComponent<JWTProp> {

    private final Project project;
    private final LanguageTextArea jwtTextArea;
    private final JButton resolveBtn = new JButton("Resolve", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton generateBtn = new JButton("Generate", IconC.DOUBLE_ANGLES_UP);

    private final JBTabbedPane infoTabPane;
    private final LanguageTextArea headerTextArea;
    private final LanguageTextArea payloadTextArea;

    public JWTComponent(final Project project) {
        super(project);
        this.jwtTextArea = new LanguageTextArea(project);
        this.infoTabPane = new JBTabbedPane(JBTabbedPane.TOP);
        this.headerTextArea = new LanguageTextArea(project);
        this.payloadTextArea = new LanguageTextArea(project);

        this.jwtTextArea.setPlaceholder("Here is the generated JWT or enter JWT");

        this.project = project;
        this.initLayout();
        this.initAction();
    }

    @Override
    protected TypeReference<List<JWTProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected Comparator<? super JWTProp> comparator() {
        return Comparator.comparing(JWTProp::getSorted);
    }

    @Override
    protected String cryptoPropText(final JWTProp prop) {
        return switch (prop.signAlgorithm()) {
            case HS256, HS384, HS512 -> "%s - %s [ %s / %s ]".formatted(
                    prop.getTitle(), prop.getDescription(), StringUtils.maskSensitive(prop.getSymmetricKey()), prop.symmetricKeyType()
            );
            default -> "%s - %s [ Asymmetric Crypto ]".formatted(prop.getTitle(), prop.getDescription());
        };
    }

    @Override
    protected boolean isDirectory(final JWTProp jwtProp) {
        return false;
    }

    @Override
    protected void initLayout() {
        GridBagUtils.builder(this)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightX(1).add(this.cryptoPropComboBox)
                        .newCell().weightX(0).add(this.cryptoManageBtn))
                .fill(GridBagFill.BOTH)
                .newRow(row -> row.newCell().weightY(0.5).gridWidth(2).add(
                        this.jwtTextArea.withRightBar(this.resolveBtn())
                ))
                .newRow(row -> {
                    this.infoTabPane.addTab("Header", this.headerTextArea.withRightBar());
                    this.infoTabPane.addTab("Payload", this.payloadTextArea.withRightBar());
                    row.newCell().weightY(0.5).gridWidth(2).add(this.infoTabPane);
                });
    }

    @Override
    protected void initAction() {
        this.cryptoManageBtn.addActionListener(e -> {
            final JWTPropDialog dialog = new JWTPropDialog(this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
        this.generateBtn.addActionListener(e -> {
            // generate
        });
    }

    @Override
    protected Stream<JWTProp> flatProps(final List<JWTProp> props) {
        return props.stream()
                .mapMulti((prop, consumer) -> consumer.accept(prop));
    }

    AnAction resolveBtn() {
        return new AnAction("Resolve", "Resolve JWT", IconC.GENERATE) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                final JWTProp item = cryptoPropComboBox.getItem();
                if (Objects.isNull(item)) {
                    Messages.showWarningDialog("Please select the signing key configuration item", "Resolve Failed");
                    return;
                }
                if (StringUtil.isEmpty(jwtTextArea.getText())) {
                    Messages.showWarningDialog("Please enter JWT", "Resolve Failed");
                    return;
                }
                final JwtParserBuilder builder = Jwts.parser();
                builder.verifyWith(item.secretKeySpec());
                final JwtParser build = builder.build();
                final Jwt<?, ?> parse = build.parse(jwtTextArea.getText());
                final Jwe<Claims> claimsJws = parse.accept(Jwe.CLAIMS);
//            final Jws<Claims> claimsJws = build.parseSignedClaims(jwtTextArea.getText());
                headerTextArea.setText(IGNORE_TRANSIENT_AND_NULL.toPrettyJson(claimsJws.getHeader()));
                payloadTextArea.setText(IGNORE_TRANSIENT_AND_NULL.toPrettyJson(claimsJws.getPayload()));
            }
        };
    }


}
