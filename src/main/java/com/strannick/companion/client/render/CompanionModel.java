package com.strannick.companion.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import com.strannick.companion.entity.CompanionEntity;

/**
 * Кастомная модель компаньона на основе HumanoidModel
 */
public class CompanionModel extends HumanoidModel<CompanionEntity> {
    public CompanionModel(ModelPart root) {
        super(root);
    }
}