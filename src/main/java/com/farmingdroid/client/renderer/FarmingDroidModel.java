package com.farmingdroid.client.renderer;

import com.farmingdroid.entity.FarmingDroidEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class FarmingDroidModel extends HierarchicalModel<FarmingDroidEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart thruster;

    public FarmingDroidModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.thruster = this.body.getChild("thruster");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Main Steampunk Brass Body (8x8x8 centered)
        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        // Top Chimney / Pressure valve
                        .texOffs(0, 16)
                        .addBox(-1.5F, -11.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // Steam Thruster Nozzle at bottom
        body.addOrReplaceChild("thruster",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(FarmingDroidEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Visual hovering bobbing animation
        float bob = Mth.sin(ageInTicks * 0.1F) * 0.5F;
        this.body.y = 18.0F + bob;

        // Subtle tilt during movement
        this.body.xRot = limbSwingAmount * 0.2F;
        this.body.yRot = netHeadYaw * ((float) Math.PI / 180F);
    }
}
