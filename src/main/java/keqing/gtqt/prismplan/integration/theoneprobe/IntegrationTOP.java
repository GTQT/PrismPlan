package keqing.gtqt.prismplan.integration.theoneprobe;


import mcjty.theoneprobe.TheOneProbe;

public class IntegrationTOP {

    public static void registerProvider() {
        TheOneProbe.theOneProbeImp.registerProvider(EStorageInfoProvider.INSTANCE);
    }

}
