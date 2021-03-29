package code
/**
 * This software is confidential. Test Software Inc., or one of its subsidiaries, has supplied this software to you
 * under terms of a license agreement, nondisclosure agreement or both.
 *
 */
def txt
switch (aType) {
    case "Test_Current":
        txt = "Test Current"
        break
    case "Test_History":
        txt = "Test Histroy"
        break
    default:
        txt = " "
}
return txt