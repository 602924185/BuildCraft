/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.mj;

import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjBattery;

public class MjRedstoneBatteryReceiver extends MjBatteryReceiver implements IMjRedstoneReceiver {
    public MjRedstoneBatteryReceiver(MjBattery battery) {
        super(battery);
    }
}
