import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import AppLayout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import UserList from './pages/system/UserList';
import RoleList from './pages/system/RoleList';
import PermissionList from './pages/system/PermissionList';
import ProviderList from './pages/provider/ProviderList';
import StaffList from './pages/staff/StaffList';
import ElderList from './pages/elder/ElderList';
import OrderList from './pages/order/OrderList';
import SettlementList from './pages/financial/SettlementList';
import EvaluationList from './pages/evaluation/EvaluationList';
import DictTypeList from './pages/config/DictTypeList';
import AreaList from './pages/config/AreaList';
import ServiceTypeList from './pages/config/ServiceTypeList';
import OrderStatistics from './pages/statistics/OrderStatistics';
import FinancialStatistics from './pages/statistics/FinancialStatistics';

const isAuthenticated = () => {
  return !!localStorage.getItem('token');
};

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="system/users" element={<UserList />} />
            <Route path="system/roles" element={<RoleList />} />
            <Route path="system/permissions" element={<PermissionList />} />
            <Route path="provider" element={<ProviderList />} />
            <Route path="staff" element={<StaffList />} />
            <Route path="elder" element={<ElderList />} />
            <Route path="order" element={<OrderList />} />
            <Route path="financial" element={<SettlementList />} />
            <Route path="evaluation" element={<EvaluationList />} />
            <Route path="config/dict-types" element={<DictTypeList />} />
            <Route path="config/areas" element={<AreaList />} />
            <Route path="config/service-types" element={<ServiceTypeList />} />
            <Route path="statistics/order" element={<OrderStatistics />} />
            <Route path="statistics/financial" element={<FinancialStatistics />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
};

export default App;
