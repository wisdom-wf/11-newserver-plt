// User and Authentication Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface UserInfo {
  userId: number;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  gender?: string;
  avatar?: string;
  userType?: string;
  tenantId?: string;
  roles: string[];
  permissions: string[];
  status?: number;
  createTime?: string;
}

// Provider Types
export interface Provider {
  id: number;
  name: string;
  code: string;
  businessLicense: string;
  legalPerson: string;
  contactPhone: string;
  address: string;
  serviceScope: string[];
  status: number;
  level: number;
  regionCode: string;
  createTime: string;
  updateTime: string;
}

export interface ProviderQuery {
  name?: string;
  code?: string;
  status?: number;
  level?: number;
  regionCode?: string;
  page: number;
  pageSize: number;
}

// Staff Types
export interface Staff {
  id: number;
  name: string;
  idCard: string;
  phone: string;
  gender: number;
  age: number;
  providerId: number;
  providerName: string;
  serviceTypes: string[];
  serviceAreas: string[];
  certifications: string[];
  status: number;
  createTime: string;
  updateTime: string;
}

export interface StaffQuery {
  name?: string;
  phone?: string;
  providerId?: number;
  serviceType?: string;
  status?: number;
  page: number;
  pageSize: number;
}

// Elder Types
export interface Elder {
  id: number;
  name: string;
  idCard: string;
  phone: string;
  gender: number;
  age: number;
  address: string;
  careLevel: number;
  familyPhone?: string;
  familyContact?: string;
  subsidyType?: number;
  subsidyAmount?: number;
  healthInfo?: string;
  status: number;
  regionCode: string;
  createTime: string;
  updateTime: string;
}

export interface ElderQuery {
  name?: string;
  idCard?: string;
  careLevel?: number;
  subsidyType?: number;
  status?: number;
  regionCode?: string;
  page: number;
  pageSize: number;
}

// Order Types
export interface Order {
  id: number;
  orderNo: string;
  elderId: number;
  elderName: string;
  elderPhone: string;
  elderAddress: string;
  providerId: number;
  providerName: string;
  staffId?: number;
  staffName?: string;
  serviceType: string;
  serviceContent: string;
  serviceTime: string;
  serviceDuration: number;
  serviceFee: number;
  subsidyFee: number;
  selfPayFee: number;
  status: number;
  remark?: string;
  createTime: string;
  updateTime: string;
}

export interface OrderQuery {
  orderNo?: string;
  elderName?: string;
  providerId?: number;
  staffId?: number;
  serviceType?: string;
  status?: number;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Financial Types
export interface Financial {
  id: number;
  settlementNo: string;
  orderId: number;
  orderNo: string;
  providerId: number;
  providerName: string;
  elderName: string;
  serviceType: string;
  serviceFee: number;
  subsidyFee: number;
  selfPayFee: number;
  settlementAmount: number;
  settlementStatus: number;
  settlementTime?: string;
  paymentTime?: string;
  paymentMethod?: string;
  createTime: string;
  updateTime: string;
}

export interface FinancialQuery {
  settlementNo?: string;
  providerId?: number;
  elderName?: string;
  settlementStatus?: number;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Evaluation Types
export interface Evaluation {
  id: number;
  orderId: number;
  orderNo: string;
  elderId: number;
  elderName: string;
  providerId: number;
  providerName: string;
  staffId: number;
  staffName: string;
  serviceType: string;
  serviceScore: number;
  attitudeScore: number;
  skillScore: number;
  punctualityScore: number;
  overallScore: number;
  content?: string;
  images?: string[];
  reply?: string;
  replyTime?: string;
  createTime: string;
}

export interface EvaluationQuery {
  orderNo?: string;
  elderName?: string;
  providerId?: number;
  staffId?: number;
  serviceType?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Config Types
export interface Config {
  id: number;
  category: string;
  code: string;
  name: string;
  value: string;
  sort: number;
  status: number;
  remark?: string;
  createTime: string;
  updateTime: string;
}

// Statistics Types
export interface DashboardStats {
  totalProviders: number;
  activeProviders: number;
  totalStaff: number;
  activeStaff: number;
  totalElders: number;
  activeElders: number;
  totalOrders: number;
  completedOrders: number;
  pendingOrders: number;
  totalRevenue: number;
  totalSubsidy: number;
  totalSelfPay: number;
  todayOrders: number;
  todayRevenue: number;
  monthOrders: number;
  monthRevenue: number;
}

export interface StatisticsQuery {
  startDate?: string;
  endDate?: string;
  regionCode?: string;
  providerId?: number;
}

export interface ChartData {
  date: string;
  value: number;
  label?: string;
}

// System Types
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  permissions: string[];
  status: number;
  createTime: string;
  updateTime: string;
}

export interface Permission {
  id: number;
  name: string;
  code: string;
  type: number;
  parentId?: number;
  path?: string;
  icon?: string;
  sort: number;
  status: number;
}

// API Response Types
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

// Menu Types
export interface MenuItem {
  key: string;
  label: string;
  icon?: React.ReactNode;
  children?: MenuItem[];
}
