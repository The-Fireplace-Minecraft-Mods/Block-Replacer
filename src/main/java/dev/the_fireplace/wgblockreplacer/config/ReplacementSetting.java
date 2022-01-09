package dev.the_fireplace.wgblockreplacer.config;

import com.google.common.collect.Lists;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.Config;
import dev.the_fireplace.wgblockreplacer.WGBRConstants;

import java.util.List;

public final class ReplacementSetting implements Config
{
    public static final String SUBFOLDER = WGBRConstants.MODID + "_replacements";
    public static final short ABSOLUTE_MINIMUM_Y = (short) -4000;
    public static final short ABSOLUTE_MAXIMUM_Y = (short) 4000;

    private String id;
    private String targetBlockId;
    private String replacementBlockId;
    private double baseReplacementChance;

    private boolean multiplyChanceByHeight;
    private short minimumY;
    private short maximumY;

    private List<String> biomeList;
    private List<String> dimensionList;

    private short lateReplacementTicks;

    public ReplacementSetting(ConfigStateManager configStateManager, String id) {
        this.id = id;
        configStateManager.initialize(this);
    }

    @Override
    public String getSubfolderName() {
        return SUBFOLDER;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        targetBlockId = buffer.readString("targetBlockId", "minecraft:stone");
        replacementBlockId = buffer.readString("replacementBlockId", "minecraft:stone");
        baseReplacementChance = buffer.readDouble("baseReplacementChance", 1.0);
        multiplyChanceByHeight = buffer.readBool("multiplyChanceByHeight", false);
        minimumY = buffer.readShort("minimumY", ABSOLUTE_MINIMUM_Y);
        maximumY = buffer.readShort("maximumY", ABSOLUTE_MAXIMUM_Y);
        lateReplacementTicks = buffer.readShort("lateReplacementTicks", (short) 0);
        biomeList = buffer.readStringList("biomeList", Lists.newArrayList("*"));
        dimensionList = buffer.readStringList("dimensionList", Lists.newArrayList("*"));
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("targetBlockId", targetBlockId);
        buffer.writeString("replacementBlockId", replacementBlockId);
        buffer.writeDouble("baseReplacementChance", baseReplacementChance);
        buffer.writeBool("multiplyChanceByHeight", multiplyChanceByHeight);
        buffer.writeShort("minimumY", minimumY);
        buffer.writeShort("maximumY", maximumY);
        buffer.writeShort("lateReplacementTicks", lateReplacementTicks);
        buffer.writeStringList("biomeList", biomeList);
        buffer.writeStringList("dimensionList", dimensionList);
    }

    public String getTargetBlockId() {
        return targetBlockId;
    }

    public void setTargetBlockId(String targetBlockId) {
        this.targetBlockId = targetBlockId;
    }

    public String getReplacementBlockId() {
        return replacementBlockId;
    }

    public void setReplacementBlockId(String replacementBlockId) {
        this.replacementBlockId = replacementBlockId;
    }

    public double getBaseReplacementChance() {
        return baseReplacementChance;
    }

    public void setBaseReplacementChance(double baseReplacementChance) {
        this.baseReplacementChance = baseReplacementChance;
    }

    public boolean isMultiplyChanceByHeight() {
        return multiplyChanceByHeight;
    }

    public void setMultiplyChanceByHeight(boolean multiplyChanceByHeight) {
        this.multiplyChanceByHeight = multiplyChanceByHeight;
    }

    public short getMinimumY() {
        return minimumY;
    }

    public void setMinimumY(short minimumY) {
        this.minimumY = minimumY;
    }

    public short getMaximumY() {
        return maximumY;
    }

    public void setMaximumY(short maximumY) {
        this.maximumY = maximumY;
    }

    public List<String> getBiomeList() {
        return biomeList;
    }

    public void setBiomeList(List<String> biomeList) {
        this.biomeList = biomeList;
    }

    public List<String> getDimensionList() {
        return dimensionList;
    }

    public void setDimensionList(List<String> dimensionList) {
        this.dimensionList = dimensionList;
    }

    public short getLateReplacementTicks() {
        return lateReplacementTicks;
    }

    public void setLateReplacementTicks(short lateReplacementTicks) {
        this.lateReplacementTicks = lateReplacementTicks;
    }
}
