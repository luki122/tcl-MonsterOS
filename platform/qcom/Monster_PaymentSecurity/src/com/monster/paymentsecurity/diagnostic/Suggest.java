package com.monster.paymentsecurity.diagnostic;

/**建议
 * Created by logic on 16-11-28.
 */
public class Suggest implements Action {

    private Action innerAction;

    @Override
    public boolean run() {
        return innerAction != null && innerAction.run();
    }

    public void setAction(Action suggestAction){
        this.innerAction = suggestAction;
    }
}
