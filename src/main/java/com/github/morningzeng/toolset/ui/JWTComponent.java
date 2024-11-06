package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.dialog.JWTPropDialog;
import com.github.morningzeng.toolset.model.JWTProp;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.openapi.project.Project;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

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

    private final LabelTextArea headerTextArea = new LabelTextArea("Header");
    private final LabelTextArea payloadTextArea = new LabelTextArea("Payload");

    public JWTComponent(final Project project) {
        super(project);
        this.jwtTextArea = new LanguageTextArea(project);
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
            case NONE -> "";
            case HS256, HS384, HS512 -> "%s - %s [ %s / %s ]".formatted(
                    prop.getTitle(), prop.getDesc(), StringUtils.maskSensitive(prop.getSymmetricKey()), prop.symmetricKeyType()
            );
            default -> "%s - %s [ Asymmetric Crypto ]".formatted(prop.getTitle(), prop.getDesc());
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
                .newRow(row -> {

                })
                .newRow(row -> {
                });
    }

    @Override
    protected void initAction() {
        this.cryptoManageBtn.addActionListener(e -> {
            final JWTPropDialog dialog = new JWTPropDialog(this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
        this.resolveBtn.addActionListener(e -> {
            // resolve
            final JWTProp item = this.cryptoPropComboBox.getItem();
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
        this.generateBtn.addActionListener(e -> {
            // generate
        });
    }

    @Override
    protected Stream<JWTProp> flatProps(final List<JWTProp> props) {
        return Stream.empty();
    }

}
