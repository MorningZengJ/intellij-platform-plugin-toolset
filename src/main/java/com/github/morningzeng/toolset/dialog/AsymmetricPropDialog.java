package com.github.morningzeng.toolset.dialog;

import cn.hutool.crypto.asymmetric.KeyType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.dialog.AsymmetricPropDialog.RightPanel;
import com.github.morningzeng.toolset.model.AsymmetricCryptoProp;
import com.github.morningzeng.toolset.model.Pair;
import com.github.morningzeng.toolset.proxy.InitializingBean;
import com.github.morningzeng.toolset.utils.ArrayUtils;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagBuilder;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Morning Zeng
 * @since 2024-11-01
 */
@Slf4j
public final class AsymmetricPropDialog extends AbstractPropDialog<AsymmetricCryptoProp, RightPanel> {

    private final AsymmetricCrypto crypto;

    public AsymmetricPropDialog(final AsymmetricCrypto crypto, final @Nullable Project project, final Consumer<List<AsymmetricCryptoProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
        init();
        setTitle("Asymmetric Properties");
        this.crypto = crypto;
    }

    @Override
    AnAction[] barActions() {
        return ArrayUtils.merge(AnAction[]::new, super.barActions(), this.generateBtn());
    }

    @Override
    Predicate<AsymmetricCryptoProp> enabledNode() {
        return prop -> crypto.equals(prop.getCrypto());
    }

    @Override
    TypeReference<List<AsymmetricCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    AsymmetricCryptoProp generateBean(final String name, final boolean isGroup) {
        return AsymmetricCryptoProp.builder().title(name).directory(isGroup).crypto(crypto).build();
    }

    @Override
    void writeProp(final AsymmetricCryptoProp prop, final RightPanel rightPanel) {
        prop.setTitle(rightPanel.titleTextField.getText())
                .setKey(rightPanel.keyTextArea.getText())
                .setIsPublicKey(rightPanel.keyTypeCombo.getSelectedItem() == KeyType.PublicKey)
                .setDescription(rightPanel.descTextArea.getText());
    }

    @Override
    RightPanel createRightItemPanel(final AsymmetricCryptoProp prop) {
        return InitializingBean.create(
                RightPanel.class,
                Pair.of(Project.class, this.project),
                Pair.of(prop.getClass(), prop)
        );
    }

    AnAction generateBtn() {
        return new AnAction("Generate", "Generate key pair", IconC.GENERATE) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                final Pair<String, String> cryptoKeyPair = crypto.genKey();
                final List<TreeNode> nodes = Lists.newArrayList(tree.getRoot().children().asIterator());
                final String group = "%s (Generate)".formatted(crypto.name());
                final Optional<DefaultMutableTreeNode> dirOpt = nodes.stream()
                        .filter(node -> node instanceof DefaultMutableTreeNode)
                        .map(node -> (DefaultMutableTreeNode) node)
                        .filter(node -> {
                            final AsymmetricCryptoProp prop = tree.getNodeValue(node);
                            return group.equals(prop.getTitle());
                        })
                        .findFirst();
                final DefaultMutableTreeNode generate = dirOpt.orElseGet(() -> {
                    tree.clearSelection();
                    return tree.create(generateBean(group, true), true);
                });
                TreeUtil.selectNode(tree, generate);
                final String description = "Plugin generates %s".formatted(crypto.name());
                final AsymmetricCryptoProp publicKey = generateBean("PublicKey", false)
                        .setIsPublicKey(true)
                        .setKey(cryptoKeyPair.key())
                        .setCrypto(crypto)
                        .setDescription(description);
                tree.create(publicKey, false);
                tree.setSelectionPath(new TreePath(generate));
                final AsymmetricCryptoProp privateKey = generateBean("PrivateKey", false)
                        .setIsPublicKey(false)
                        .setKey(cryptoKeyPair.value())
                        .setCrypto(crypto)
                        .setDescription(description);
                tree.create(privateKey, false);
            }
        };
    }

    static final class RightPanel extends AbstractRightPanel<AsymmetricCryptoProp> {
        private final ComboBox<KeyType> keyTypeCombo = new ComboBox<>(new KeyType[]{KeyType.PublicKey, KeyType.PrivateKey});
        private final LabelTextArea keyTextArea;
        private final LabelTextArea descTextArea;

        RightPanel(final Project project, final AsymmetricCryptoProp prop) {
            super(prop);
            this.keyTextArea = new LabelTextArea(project, "Key");
            this.descTextArea = new LabelTextArea(project, "Desc");
        }

        @Override
        protected Consumer<GridBagBuilder<AbstractRightPanel<AsymmetricCryptoProp>>> itemLayout() {
            return builder -> {
                this.keyTextArea.setText(prop.getKey());
                this.keyTypeCombo.setSelectedItem(Objects.isNull(prop.getIsPublicKey()) || prop.getIsPublicKey() ? KeyType.PublicKey : KeyType.PrivateKey);
                this.descTextArea.setText(prop.getDescription());

                builder.newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                                .newCell().weightX(1).add(this.titleTextField)
                                .newCell().weightX(0).add(this.keyTypeCombo))
                        .newRow(row -> row.fill(GridBagFill.BOTH)
                                .newCell().weightX(1).weightY(1).gridWidth(2).add(this.keyTextArea))
                        .newRow(row -> row.newCell().gridWidth(2).add(this.descTextArea));
            };
        }
    }
}
