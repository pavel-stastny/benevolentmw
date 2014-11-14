package cz.incad.kramerius.security.impl.criteria;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.NKPDataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class BenevolentMovingWallTest {

    // Drobnustky
    @Test
    public void testMW1() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String requestedPID = NKPDataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }

    @Test
    public void testMW2() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String requestedPID = NKPDataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.NOT_APPLICABLE);
    }

    //Drobnustky stranka
    @Test
    public void testMW3() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String requestedPID = NKPDataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }

    //Drobnustky stranka
    @Test
    public void testMW4() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String requestedPID = NKPDataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.NOT_APPLICABLE);
    }
    
    
    @Test
    public void testMW5() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String requestedPID = "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6";// volume;
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.NOT_APPLICABLE);
    }

    
    public EvaluatingResult mw(String movingWallFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getBiblioMods")
        .addMockedMethod("getDC")
        .createMock();
        
        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(NKPDataPrepare.fedoraProfile33());
        NKPDataPrepare.drobnustkyMODS(fa33);
        NKPDataPrepare.drobnustkyDCS(fa33);
 
        NKPDataPrepare.narodniListyMods(fa33);
        NKPDataPrepare.narodniListyDCs(fa33);
 
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = NKPDataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { NKPDataPrepare.PATHS_MAPPING.get(key)}).anyTimes();
        }
        
        replay(fa33, solrAccess,acLog);

        RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
        contextFactory.setFedoraAccess(fa33);
        contextFactory.setSolrAccess(solrAccess);

        RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1");
        BenevolentMovingWall wall = new BenevolentMovingWall();
        wall.setCriteriumParamValues(new Object[] {movingWallFromGUI});
        wall.setEvaluateContext(context);

        EvaluatingResult evaluated = wall.evalute();
        return evaluated;
    }
}
