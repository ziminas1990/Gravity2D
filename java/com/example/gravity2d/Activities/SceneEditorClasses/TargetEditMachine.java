package com.example.gravity2d.Activities.SceneEditorClasses;

import android.os.Bundle;

import com.example.gravity2d.Activities.Common.StateMachine;
import com.example.gravity2d.ModelObjects.ModelTarget;

/**
 * Конечный автомат для редактирования мишени
 * @author ZiminAS on 12.04.2014.
 */
public class TargetEditMachine extends StateMachine {
    public static long MACHINE_TYPE_ID = 3;

    public static long stateStart = 0;
    public static long stateFirstPoint = 1;
    public static long stateSecondPoint = 2;

    private ModelTarget mTarget;

    TargetEditMachine() {
        super(MACHINE_TYPE_ID);
        super.mTag = "[TargetEditMachine]";
        reset();
    }

    public void saveToBundle(Bundle data, String prefix) {
        super.saveToBundle(data, prefix + "[StateMachine]");
        data.putSerializable("mTarget", mTarget);
    }

    public void loadFromBundle(Bundle data, String prefix) {
        super.loadFromBundle(data, prefix + "[StateMachine]");
        mTarget = (ModelTarget)data.getSerializable("mTarget");
    }

    public boolean reset() {
        mTarget = new ModelTarget();
        super.setState(stateStart);
        return true;
    }

    public ModelTarget getTarget() { return mTarget; }
}
