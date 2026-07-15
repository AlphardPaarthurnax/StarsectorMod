package com.github.alphardpaarthurnax.script.data;

public class AsteroidsData {
    public static final float minRingRadius = 2048f;
    public enum randomFlag {
        NumAsteroids(0x1),
        Radius(0x10),
        Width(0x100),
        Period(0x1000),
        PeriodOffset(0x10000);
        private final int flag;

        randomFlag(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }

        public boolean check(int in) {
            return (in & flag) != 0;
        }
    }
    public enum RingType {
        Asteroids("rings_asteroids0", 4),
        Dust("rings_dust0", 4),
        Ice("rings_ice0", 4),
        Special("rings_special0", 2),
        Test("ringtest",1);

        private final String name;
        private final int blockSize;

        RingType(String name, int blockSize) {
            this.name = name;
            this.blockSize = blockSize;
        }

        public String getType() {
            return name;
        }

        public int getBlockSize() {
            return blockSize;
        }
    }
}