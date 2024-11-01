package com.github.morningzeng.toolset.dialog;

import cn.hutool.crypto.asymmetric.KeyType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.model.AsymmetricCryptoProp;
import com.github.morningzeng.toolset.model.Pair;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-11-01
 */
public final class AsymmetricPropDialog extends AbstractPropDialog<AsymmetricCryptoProp> {

    private final AsymmetricCrypto crypto;
    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final ComboBox<KeyType> keyTypeCombo = new ComboBox<>(new KeyType[]{KeyType.PublicKey, KeyType.PrivateKey});
    private final LabelTextArea keyTextArea = new LabelTextArea("Key");
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public AsymmetricPropDialog(final AsymmetricCrypto crypto, final @Nullable Project project, final Consumer<List<AsymmetricCryptoProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
        init();
        setTitle("Asymmetric Properties");
        this.crypto = crypto;
    }

    @Override
    AnAction[] barActions() {
        return Stream.concat(
                Arrays.stream(super.barActions()),
                Stream.of(
                        new AnAction("Generate", "Generate key pair", IconC.AUTORENEW) {
                            @Override
                            public void actionPerformed(@NotNull final AnActionEvent e) {
                                final Pair<String, String> cryptoKeyPair = crypto.genKey();
                                final List<TreeNode> nodes = Lists.newArrayList(tree.getRoot().children().asIterator());
                                final Optional<DefaultMutableTreeNode> dirOpt = nodes.stream()
                                        .filter(node -> node instanceof DefaultMutableTreeNode)
                                        .map(node -> (DefaultMutableTreeNode) node)
                                        .filter(node -> {
                                            final AsymmetricCryptoProp prop = tree.getNodeValue(node);
                                            return "Generate".equals(prop.getTitle());
                                        })
                                        .findFirst();
                                final DefaultMutableTreeNode generate = dirOpt.orElseGet(() -> {
                                    tree.clearSelection();
                                    return tree.create(generateBean("Generate", true), true);
                                });
                                tree.setSelectionPath(new TreePath(generate));
                                final AsymmetricCryptoProp publicKey = generateBean("PublicKey", false)
                                        .setIsPublicKey(true)
                                        .setKey(cryptoKeyPair.key())
                                        .setCrypto(crypto)
                                        .setDescription("Plug-in generation");
                                tree.create(publicKey, false);
                                tree.setSelectionPath(new TreePath(generate));
                                final AsymmetricCryptoProp privateKey = generateBean("PrivateKey", false)
                                        .setIsPublicKey(false)
                                        .setKey(cryptoKeyPair.value())
                                        .setCrypto(crypto)
                                        .setDescription("Plug-in generation");
                                tree.create(privateKey, false);
                            }
                        }
                )
        ).toArray(AnAction[]::new);
    }

    @Override
    TypeReference<List<AsymmetricCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    AsymmetricCryptoProp generateBean(final String name, final boolean isGroup) {
        return AsymmetricCryptoProp.builder().title(name).directory(isGroup).build();
    }

    @Override
    void writeProp() {
        final AsymmetricCryptoProp prop = this.tree.getSelectedValue();
        if (Objects.isNull(prop) || prop.isDirectory()) {
            return;
        }
        prop.setTitle(this.titleTextField.getText())
                .setKey(this.keyTextArea.getText())
                .setIsPublicKey(this.keyTypeCombo.getSelectedItem() == KeyType.PublicKey)
                .setDescription(this.descTextArea.getText());
        this.tree.reloadTree((TreeNode) this.tree.getLastSelectedPathComponent());

    }

    @Override
    void createRightPanel(final AsymmetricCryptoProp prop) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(prop)) {
            return;
        }
        this.titleTextField.setText(prop.getTitle());
        this.keyTextArea.setText(prop.getKey());
        this.keyTypeCombo.setSelectedItem(Objects.isNull(prop.getIsPublicKey()) || prop.getIsPublicKey() ? KeyType.PublicKey : KeyType.PrivateKey);
        this.descTextArea.setText(prop.getDescription());

        GridBagUtils.builder(panel)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightX(1).add(this.titleTextField)
                        .newCell().weightX(0).add(this.keyTypeCombo))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightX(1).weightY(1).gridWidth(2).add(this.keyTextArea))
                .newRow(row -> row.newCell().gridWidth(2).add(this.descTextArea));
    }
}
