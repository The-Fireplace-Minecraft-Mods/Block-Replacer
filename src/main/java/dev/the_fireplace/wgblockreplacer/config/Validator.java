package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.wgblockreplacer.api.config.BlockRiskAssessor;
import dev.the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import dev.the_fireplace.wgblockreplacer.api.config.ConfigValidator;
import dev.the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Validator implements ConfigValidator {
    @Deprecated
    public static final Validator INSTANCE = new Validator();

    private final Set<String> errors = new HashSet<>();
    private final Set<String> warnings = new HashSet<>();
    private boolean isValid = false;
    private boolean hasCalculated = false;
    private Validator() {}

    @Override
    public boolean calculateValidity() {
        errors.clear();
        errors.addAll(validateArrayLengths());
        errors.addAll(validateBiomes());
        errors.addAll(validateBlocks());
        warnings.clear();
        warnings.addAll(getNonstandardMetaWarnings());

        isValid = errors.isEmpty();
        hasCalculated = true;

        return isValid;
    }

    @Override
    public boolean isValid() {
        if (!hasCalculated) {
            return calculateValidity();
        }

        return isValid;
    }

    @Override
    public Collection<String> getValidationErrors() {
        Set<String> messages = new HashSet<>();
        messages.addAll(errors);
        messages.addAll(warnings);

        return messages;
    }

    private Collection<String> validateArrayLengths() {
        Set<String> errors = new HashSet<>(9);
        int maxLength = max(
            ConfigAccess.getInstance().getReplaceBlockIds().length,
            ConfigAccess.getInstance().getReplaceWithBlockIds().length,
            ConfigAccess.getInstance().getReplaceWithMetas().length,
            ConfigAccess.getInstance().getReplaceBlockMetas().length,
            ConfigAccess.getInstance().getReplaceChances().length,
            ConfigAccess.getInstance().getDimensionLists().length,
            ConfigAccess.getInstance().getMultiplyChances().length,
            ConfigAccess.getInstance().getMinYs().length,
            ConfigAccess.getInstance().getMaxYs().length,
            ConfigAccess.getInstance().getBiomeFilterLists().length
        );
        if (ConfigAccess.getInstance().getReplaceBlockIds().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblock", ConfigAccess.getInstance().getReplaceBlockIds().length, maxLength));
        if (ConfigAccess.getInstance().getReplaceWithBlockIds().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewith", ConfigAccess.getInstance().getReplaceWithBlockIds().length, maxLength));
        if (ConfigAccess.getInstance().getReplaceWithMetas().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewithmeta", ConfigAccess.getInstance().getReplaceWithMetas().length, maxLength));
        if (ConfigAccess.getInstance().getReplaceBlockMetas().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblockmeta", ConfigAccess.getInstance().getReplaceBlockMetas().length, maxLength));
        if (ConfigAccess.getInstance().getReplaceChances().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacepercent", ConfigAccess.getInstance().getReplaceChances().length, maxLength));
        if (ConfigAccess.getInstance().getDimensionLists().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "dimension_list", ConfigAccess.getInstance().getDimensionLists().length, maxLength));
        if (ConfigAccess.getInstance().getMultiplyChances().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "multiplychance", ConfigAccess.getInstance().getMultiplyChances().length, maxLength));
        if (ConfigAccess.getInstance().getMinYs().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "miny", ConfigAccess.getInstance().getMinYs().length, maxLength));
        if (ConfigAccess.getInstance().getMaxYs().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "maxy", ConfigAccess.getInstance().getMaxYs().length, maxLength));
        if (ConfigAccess.getInstance().getBiomeFilterLists().length < maxLength)
            errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "biomefilter", ConfigAccess.getInstance().getBiomeFilterLists().length, maxLength));

        return errors;
    }

    private Collection<String> validateBiomes() {
        Set<String> errors = new HashSet<>();

        for (String biomeList: ConfigAccess.getInstance().getBiomeFilterLists()) {
            for (String biome : biomeList.split(",")) {
                if (biome.equals("*")) {
                    continue;
                }
                try {
                    if (Biome.REGISTRY.getObject(new ResourceLocation(biome)) != null) {
                        continue;
                    }
                } catch (Exception ignored) {}
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.biome_not_found", biome));
            }
        }

        return errors;
    }

    private Collection<String> validateBlocks() {
        Set<String> errors = new HashSet<>();

        errors.addAll(validateBlockLists(ConfigAccess.getInstance().getReplaceBlockIds()));
        errors.addAll(validateBlockLists(ConfigAccess.getInstance().getReplaceWithBlockIds()));
        errors.addAll(validateBlockRiskiness(ConfigAccess.getInstance().getReplaceWithBlockIds()));

        return errors;
    }

    private Collection<String> validateBlockLists(String[] blockLists) {
        Set<String> errors = new HashSet<>();

        for (String blockListString: blockLists) {
            String[] blockList = blockListString.split(",");
            for (String blockId: blockList) {
                if (Block.getBlockFromName(blockId) == null) {
                    errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.block_not_found", blockId));
                }
            }
        }

        return errors;
    }

    private Collection<String> validateBlockRiskiness(String[] blockLists) {
        Set<String> errors = new HashSet<>();

        if (ConfigAccess.getInstance().allowRiskyBlocks()) {
            return errors;
        }

        for (String blockListString: blockLists) {
            String[] blockList = blockListString.split(",");
            for (String blockId: blockList) {
                Block block = Block.getBlockFromName(blockId);
                if (block == null) {
                    continue;
                }

                if (BlockRiskAssessor.getInstance().isRisky(block)) {
                    errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.disallowed_block", blockId));
                }
            }
        }

        return errors;
    }

    private Collection<String> getNonstandardMetaWarnings()
    {
        ArrayList<String> warnings = new ArrayList<>();

        warnings.addAll(validateBlockMetas(ConfigAccess.getInstance().getReplaceBlockMetas()));
        warnings.addAll(validateBlockMetas(ConfigAccess.getInstance().getReplaceWithMetas()));

        return warnings;
    }

    private Collection<String> validateBlockMetas(int[] blockMetas) {
        Set<String> warnings = new HashSet<>();

        for (int meta: blockMetas) {
            if (meta < -1 || meta > 15) {
                warnings.add(SimpleTranslationUtil.getStringTranslation("wgbr.blockmeta_out_of_range", meta));
            }
        }

        return warnings;
    }

    private int max(int... args) {
        int max = Integer.MIN_VALUE;
        for (int arg: args) {
            if (arg > max) {
                max = arg;
            }
        }
        return max;
    }
}
